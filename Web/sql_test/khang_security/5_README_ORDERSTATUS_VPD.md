# 5. Trạng thái đơn hàng (OrderStatusController.java & BillServiceImpl.java) - Mức độ Vừa
**Kỹ thuật Oracle:** VPD Policy (Lọc trên bảng BILL theo customer_id)

**Tác động lên Java:**
Tính năng xem lịch sử mua hàng, trạng thái đơn của khách hàng trên giao diện. Thay vì phải chèn luồng kiểm tra `customerId` ở Repository để lấy đúng Bill của user đang đăng nhập, VPD làm điều đó ở lõi CSDL thông qua `VpdContextFilter` (tương tự Giỏ Hàng và Địa Chỉ).

**Chỗ áp dụng trong Java:**

### 1. File `OrderStatusController.java`
Nơi tiếp nhận Request `/cart-status`. Controller chỉ đơn giản là gọi xuống Service mà không cần bận tâm user nào đang đăng nhập.

### 2. File `BillServiceImpl.java`
Hàm `getBillByAccount` và `getBillByStatus` được tối giản:
```java
// === CODE JAVA CŨ (Đã comment) ===
// Long customerId = account.getCustomer().getId();
// return billRepository.getBillByAccount(customerId, pageable);

// === CODE JAVA MỚI (Áp dụng Oracle VPD) ===
// Gỡ bỏ hoàn toàn việc tìm kiếm customerId
return billRepository.getBillByAccount(pageable); 
```

### 3. File `BillRepository.java`
Các câu truy vấn SQL đã bị xóa bỏ điều kiện `WHERE customer_id = ...`.
```java
// Câu Query đã xóa bỏ điều kiện lọc theo khách hàng, 
// Phó mặc cho Oracle VPD tự nối thêm "WHERE customer_id = [user_hien_tai]"
@Query(value = "SELECT * FROM (...) q ORDER BY q.create_date DESC", nativeQuery = true)
Page<Bill> getBillByAccount(Pageable pageable); 
```
