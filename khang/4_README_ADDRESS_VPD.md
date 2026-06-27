# 4. Địa chỉ giao hàng (AddressShippingServiceImpl.java) - Mức độ Vừa
**Kỹ thuật Oracle:** VPD Policy (Theo customer_id)

**Tác động lên Java:**
Tương tự như giỏ hàng, vì hệ thống đã cấu hình `VpdContextFilter` để truyền `account_id` xuống Database ở đầu mỗi Request, mọi câu lệnh lấy danh sách địa chỉ (`SELECT * FROM address_shipping`) sẽ bị Oracle can thiệp trực tiếp. Oracle tự động chèn thêm điều kiện `WHERE customer_id = (SYS_CONTEXT(...))` để đảm bảo user nào chỉ thấy địa chỉ của user đó.

**Chỗ áp dụng trong Java:**
Mở file `AddressShippingServiceImpl.java` (hoặc `AddressShippingRepository.java`):
```java
// === CODE JAVA CŨ (Đã comment) ===
// @Query("SELECT a FROM AddressShipping a WHERE a.customer.account.id = :accountId")
// List<AddressShipping> findAllByCustomer_Account_Id(Long accountId);

// === CODE JAVA MỚI (Áp dụng Oracle VPD) ===
// Không cần WHERE trong Java nữa! Chỉ cần gọi hàm findAll() gốc của JPA.
// Lệnh chạy xuống Oracle sẽ tự động bị cắt xén dữ liệu.

// Trong AddressShippingServiceImpl.java:
List<AddressShipping> addressShippings = addressShippingRepository.findAll(); 
/* VPD tự lọc theo account_id dưới DB, chống IDOR tuyệt đối */
```
