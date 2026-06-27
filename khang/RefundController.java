package com.project.WebAloTra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.WebAloTra.dto.Refund.SearchRefundDto;
import com.project.WebAloTra.entity.Payment;
import com.project.WebAloTra.repository.BillRepository;
import com.project.WebAloTra.repository.BillReturnRepository;
import com.project.WebAloTra.repository.PaymentRepository;

@Controller
public class RefundController {
    private final BillReturnRepository billReturnRepository;

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;

    public RefundController(BillReturnRepository billReturnRepository, BillRepository billRepository, PaymentRepository paymentRepository) {
        this.billReturnRepository = billReturnRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/admin-only/need-refund-mng")
    public String viewRefundPage(SearchRefundDto searchRefundDto, Model model) {
        model.addAttribute("refundList", billRepository.findListNeedRefund());
        return "/admin/refund-mng";
    }

    @PostMapping("/admin/confirm-refund/{id}")
    public String confirmRefund(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Payment payment = paymentRepository.findByOrderId(id);
        
        // ===== ÁP DỤNG ORACLE FGA (Fine-Grained Auditing) =====
        // Java chỉ thực hiện cập nhật trạng thái đơn giản, không cần ghi log thủ công.
        // Oracle FGA đã được cài đặt dưới DB (6_FGA_REFUND.sql) sẽ tự động kích hoạt
        // khi bảng PAYMENT bị UPDATE. FGA tự bắt lấy câu lệnh, user thực hiện,
        // ngày giờ và ghi vào bảng DBA_FGA_AUDIT_TRAIL siêu bảo mật.
        payment.setStatusExchange(1); 
        paymentRepository.save(payment);
        
        redirectAttributes.addFlashAttribute("successMessage", "Xác nhận hoàn " + payment.getAmount() + " cho mã giao dịch " + payment.getOrderId() + " thành công");
        return "redirect:/admin-only/need-refund-mng";
    }

}
