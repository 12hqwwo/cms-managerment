package com.project.WebAloTra.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;


import com.project.WebAloTra.config.ConfigVNPay;
import com.project.WebAloTra.dto.Payment.PaymentDto;
import com.project.WebAloTra.dto.Payment.PaymentResultDto;
import com.project.WebAloTra.entity.Payment;
import com.project.WebAloTra.repository.PaymentRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController {

    private final PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;


    public PaymentRestController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(HttpServletRequest req) throws UnsupportedEncodingException {
        // ====== CẤU HÌNH VNPAY CƠ BẢN ======
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = Integer.parseInt(req.getParameter("amount")) * 100;
        String vnp_TxnRef = ConfigVNPay.getRandomNumber(8);
        String vnp_IpAddr = ConfigVNPay.getIpAddress(req);
        String vnp_TmnCode = ConfigVNPay.vnp_TmnCode;

        // ====== LƯU PAYMENT TRƯỚC VÀO DB ======
        PaymentResultDto paymentResultDto = new PaymentResultDto();
        paymentResultDto.setTxnRef(vnp_TxnRef);
        paymentResultDto.setAmount(String.valueOf(amount / 100));
        savePaymentToDB(paymentResultDto);

        // ====== BUILD REQUEST ĐỂ GỬI LÊN VNPAY ======
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", ConfigVNPay.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Thời gian tạo và hết hạn (15 phút)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // ====== TẠO QUERY HASH & URL ======
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = ConfigVNPay.hmacSHA512(ConfigVNPay.secretKey, hashData.toString());
        String paymentUrl = ConfigVNPay.vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;

        // Trả về DTO chứa URL thanh toán
        PaymentDto paymentDto = new PaymentDto("OK", "success", paymentUrl);
        return ResponseEntity.ok(paymentDto);
    }

    // ====== HÀM LƯU PAYMENT VÀO DATABASE ======
    private void savePaymentToDB(PaymentResultDto paymentResultDto) {
        // [BẢO MẬT CƠ SỞ DỮ LIỆU: ÁP DỤNG STORED PROCEDURE]
        // Thay vì gọi paymentRepository.save(), hệ thống gọi PROC_INIT_PAYMENT 
        // để khởi tạo giao dịch an toàn trước khi chuyển hướng sang VNPay.
        /* === CODE CŨ ĐÃ COMMENT ===
        Payment payment = new Payment();
        payment.setOrderId(paymentResultDto.getTxnRef());
        payment.setAmount(paymentResultDto.getAmount());
        payment.setOrderStatus("0"); // 0 = chưa thanh toán
        payment.setStatusExchange(0);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
        === KẾT THÚC CODE CŨ === */

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_INIT_PAYMENT");
            query.registerStoredProcedureParameter("p_order_id", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_amount", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_error_code", Integer.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("p_error_msg", String.class, ParameterMode.OUT);

            query.setParameter("p_order_id", paymentResultDto.getTxnRef());
            query.setParameter("p_amount", paymentResultDto.getAmount());

            query.execute();

            Integer errorCode = (Integer) query.getOutputParameterValue("p_error_code");
            if (errorCode != null && errorCode < 0) {
                String errorMsg = (String) query.getOutputParameterValue("p_error_msg");
                throw new RuntimeException("Lỗi tạo Payment: " + errorMsg);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi PROC_INIT_PAYMENT: " + e.getMessage(), e);
        }
    }
}