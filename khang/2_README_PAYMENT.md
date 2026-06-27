# 2. Thanh toán (PaymentRestController.java & PaymentController.java) - Mức độ Dễ/Vừa
**Kỹ thuật Oracle:** PL/SQL Procedure (`PROC_INIT_PAYMENT`, `PROC_CONFIRM_PAYMENT`) & RBAC

**Tác động lên Java:**
Khi khởi tạo thanh toán hoặc nhận callback từ VNPay trả về, thay vì dùng `paymentRepository.save()` và gọi hàm tạo hóa đơn từ Service, hệ thống sẽ sử dụng trực tiếp Stored Procedure qua `EntityManager`. Điều này nhằm:
- Đảm bảo transaction an toàn tuyệt đối, tránh lỗi "race condition" lúc thanh toán.
- **Chống dội lệnh (Duplicate Callback) từ VNPay**: Nếu VNPay gọi callback thành công nhiều lần, DB chỉ xử lý đúng 1 lần duy nhất nhờ kiểm tra biến trạng thái dưới Database.

**Chỗ áp dụng trong Java:**

### 1. Khởi tạo thanh toán (`PaymentRestController.java`)
Gửi yêu cầu tới VNPay. Lưu trước trạng thái giao dịch "Chờ xử lý".
```java
// === CODE JAVA CŨ (Đã comment) ===
// Payment payment = new Payment(); 
// payment.setOrderStatus("0"); 
// paymentRepository.save(payment);

// === CODE JAVA MỚI (Áp dụng Kỹ thuật Oracle) ===
@Autowired
private EntityManager entityManager;

private void savePaymentToDB(PaymentResultDto paymentResultDto) {
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
        throw new RuntimeException("Lỗi tạo Payment: " + query.getOutputParameterValue("p_error_msg"));
    }
}
```

### 2. Nhận kết quả VNPay (`PaymentController.java`)
Nhận Callback về, tạo Hóa Đơn và Trừ Kho trên Database.
```java
// === CODE JAVA CŨ (Đã comment) ===
// cartService.orderUser(orderDto); // Có thể gây trùng đơn nếu VNPay gọi callback liên tiếp

// === CODE JAVA MỚI (Áp dụng Kỹ thuật Oracle) ===
@Autowired
private EntityManager entityManager;

@GetMapping("/payment-result")
public String viewPaymentResult(...) {
    if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
        
        // Gọi PROC_CONFIRM_PAYMENT để bảo vệ an toàn, tránh lỗi khi VNPay lặp phản hồi
        StoredProcedureQuery query = entityManager.createcreateStoredProcedureQuery("PROC_CONFIRM_PAYMENT");
        
        // Khai báo kiểu tham số (IN/OUT)
        query.registerStoredProcedureParameter("p_order_id_vnpay", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_order_details_json", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_error_code", Integer.class, ParameterMode.OUT);
        // ... (các tham số khác)
        
        // Gán tham số
        query.setParameter("p_order_id_vnpay", paymentResultDto.getTxnRef());
        query.setParameter("p_order_details_json", orderDetailsJson);
        // ... (các tham số khác)
        
        query.execute();

        Integer errorCode = (Integer) query.getOutputParameterValue("p_error_code");
        if (errorCode != null && errorCode < 0) {
            throw new RuntimeException("Lỗi xác nhận thanh toán: " + query.getOutputParameterValue("p_error_msg"));
        }
        
        // Xóa giỏ hàng trên DB (nếu cần)
    }
}
```
