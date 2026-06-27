# 6. Hoàn tiền (RefundController.java) - Mức độ Khó
**Kỹ thuật Oracle:** FGA (Fine-Grained Auditing) trên bảng PAYMENT.

**Tác động lên Java:**
Bỏ qua việc nhúng code ghi log thủ công (`System.out.println`, `Log4j` hay `auditLogService.save(...)`) trong Java. Chức năng hoàn tiền rất nhạy cảm nên Audit được đẩy xuống tận tầng DB. Dù ứng dụng Java cập nhật hay ai đó chọc thẳng vào DB (ví dụ dùng SQL Developer) để sửa bảng `Payment` thì đều bị lưu vết lại đầy đủ.

**Chỗ áp dụng trong Java:**
Mở `RefundController.java` để xem:
```java
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
        
        // ...
        return "redirect:/admin-only/need-refund-mng";
    }
```
