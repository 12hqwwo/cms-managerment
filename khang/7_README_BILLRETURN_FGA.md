# 7. Trả hàng (BillReturnController.java) - Mức độ Khó
**Kỹ thuật Oracle:** FGA (Fine-Grained Auditing) trên bảng BILL_RETURN.

**Tác động lên Java:**
Tương tự như chức năng hoàn tiền, mọi thao tác tạo mới (INSERT) hoặc cập nhật (UPDATE) phiếu trả hàng sẽ bị Oracle FGA giám sát tự động. Java không cần nhúng mã ghi log thủ công nào. Việc này giúp tránh rủi ro Developer quên ghi log, hay Hacker chọc thẳng vào Database để sửa phiếu trả hàng mà không bị phát hiện.

**Chỗ áp dụng trong Java:**
Mở `BillReturnController.java` để xem:
```java
    @PostMapping("/admin/update-bill-return-status")
    public String updateBillReturnStatus(@ModelAttribute("billReturnDto") BillReturnDto billReturnDto, Model model, RedirectAttributes redirectAttributes) {

        try {
            // ===== ÁP DỤNG ORACLE FGA (Fine-Grained Auditing) =====
            // Khi gọi hàm updateStatus(), Hibernate sẽ thực thi lệnh UPDATE trên bảng BILL_RETURN.
            // Oracle FGA đã được cài đặt (7_FGA_BILLRETURN.sql) sẽ tự động kích hoạt
            // bắt lấy thao tác UPDATE trên cột RETURN_STATUS và ghi log vào DBA_FGA_AUDIT_TRAIL.
            // Không cần phải viết thêm log lưu thủ công trong Java.
            BillReturnDto updatedBillReturn = billReturnService.updateStatus(billReturnDto.getId(), billReturnDto.getReturnStatus());
            // ...
        } catch (Exception e) {
            // ...
        }
        return "redirect:/admin-only/bill-return";
    }

    @ResponseBody
    @PostMapping("/api/bill-return")
    public BillReturnDto createBillReturn(@RequestBody BillReturnCreateDto billReturnCreateDto) {
        // ===== ÁP DỤNG ORACLE FGA (Fine-Grained Auditing) =====
        // Khi tạo phiếu trả hàng mới (INSERT vào bảng BILL_RETURN), FGA cũng sẽ bắt
        // lấy sự kiện này nếu cấu hình audit lệnh INSERT. Mọi thao tác thêm/sửa 
        // đều được tự động lưu dấu vết dưới CSDL.
        return billReturnService.createBillReturn(billReturnCreateDto);
    }
```
