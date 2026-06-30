# 9. Cập nhật trạng thái Hóa đơn (BillServiceImpl.java) - Mức độ Vừa
**Kỹ thuật Oracle:** PL/SQL Stored Procedure (`PROC_UPDATE_BILL_STATUS`)

**Tác động lên Java:**
Giống như chức năng Đặt Hàng và Thanh Toán, việc cập nhật trạng thái đơn hàng (từ Chờ xác nhận → Đang giao, v.v.) được giao phó cho Oracle Procedure xử lý thay vì dùng JPA (Hibernate).
Mục đích là để Database kiểm tra tính hợp lệ của trạng thái mới (VD: không thể đổi sang trạng thái lạ như 'DANG_NGU') và đảm bảo đồng bộ thời gian cập nhật chính xác (SYSTIMESTAMP) ở cấp độ CSDL.

**Chỗ áp dụng trong Java:**
Mở `BillServiceImpl.java`, tìm hàm `updateStatus`:
```java
// === CODE JAVA CŨ (Đã loại bỏ) ===
// Bill bill = billRepository.findById(id).orElseThrow(...);
// bill.setStatus(status);
// bill.setUpdateDate(LocalDateTime.now());
// billRepository.save(bill);

// === CODE JAVA MỚI (Gọi DB Procedure) ===
StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_UPDATE_BILL_STATUS");
query.registerStoredProcedureParameter("p_bill_id", Long.class, ParameterMode.IN);
query.registerStoredProcedureParameter("p_new_status", String.class, ParameterMode.IN);
query.registerStoredProcedureParameter("p_result_msg", String.class, ParameterMode.OUT);

// Truyền ID hóa đơn và trạng thái mới xuống DB
query.setParameter("p_bill_id", id);
query.setParameter("p_new_status", status);

query.execute(); // Oracle chạy Procedure

// Nhận kết quả từ DB trả về
String resultMsg = (String) query.getOutputParameterValue("p_result_msg");
if (!"SUCCESS".equals(resultMsg)) {
    throw new RuntimeException("Lỗi cập nhật hóa đơn: " + resultMsg);
}

// Lấy lại Bill mới nhất từ DB
return billRepository.findById(id).orElseThrow(...);
```
