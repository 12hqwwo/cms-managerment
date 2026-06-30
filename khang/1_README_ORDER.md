# 1. Đơn hàng (CartServiceImpl.java & OrderController.java) - Mức độ Dễ/Vừa
**Kỹ thuật Oracle:** PL/SQL Procedure (`PROC_CREATE_ORDER`)

**Tác động lên Java:**
Thay vì dùng nhiều lệnh JPA `save()` trong Service để tạo hóa đơn, tạo chi tiết hóa đơn, thêm lịch sử thanh toán và trừ tồn kho, Java giờ chỉ cần gom toàn bộ danh sách sản phẩm thành chuỗi JSON. Sau đó, Java gọi thẳng 1 Oracle Procedure duy nhất thông qua `EntityManager`. DB sẽ tự đảm nhận toàn bộ tính toán, kiểm tra/trừ tồn kho, và quản lý Transaction (tự động Rollback nếu có bất kỳ lỗi nào xảy ra).

**Chỗ áp dụng trong Java:**

### 1. File `OrderController.java` (Nhận Request)
Chỉ đóng vai trò chuyển tiếp dữ liệu xuống Service (đúng chuẩn mô hình MVC của Spring).
```java
@PostMapping("/api/orderUser")
public void order(@RequestBody OrderDto orderDto) {
    // Chuyển tiếp sang Service xử lý gọi Oracle Procedure
    cartService.orderUser(orderDto);
}
```

### 2. File `CartServiceImpl.java` (Gọi Procedure)
Mở hàm `orderUser(...)` và `orderAdmin(...)`:
```java
// === CODE JAVA CŨ (Đã comment lại) ===
// billRepository.save(bill);
// cho vòng lặp lưu billDetailRepository.save(item);
// productDetail.setQuantity(productDetail.getQuantity() - qty);
// productDetailRepository.save(productDetail);
// ... và nhiều logic tính toán rườm rà khác

// === CODE JAVA MỚI (Áp dụng Kỹ thuật Oracle) ===
@Autowired
private EntityManager entityManager;

@Autowired
private ObjectMapper objectMapper;

public void orderUser(OrderDto orderDto) {
    // 1. Gom danh sách chi tiết đơn hàng thành chuỗi JSON
    String orderDetailsJson = objectMapper.writeValueAsString(orderDto.getOrderDetailDtos());

    // 2. Gọi Oracle Procedure, phó mặc mọi logic transaction, trừ kho, tính tiền cho DB
    StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_CREATE_ORDER");
    
    // Khai báo kiểu tham số
    query.registerStoredProcedureParameter("p_billing_address", String.class, ParameterMode.IN);
    query.registerStoredProcedureParameter("p_order_details_json", String.class, ParameterMode.IN);
    query.registerStoredProcedureParameter("p_error_code", Integer.class, ParameterMode.OUT);
    // ... Đăng ký thêm các tham số IN/OUT khác ...

    // Truyền dữ liệu thật
    query.setParameter("p_billing_address", orderDto.getBillingAddress());
    query.setParameter("p_order_details_json", orderDetailsJson);
    
    // Thực thi
    query.execute();

    // 3. Xử lý lỗi trả về từ Oracle (nếu có)
    Integer errorCode = (Integer) query.getOutputParameterValue("p_error_code");
    if (errorCode != null && errorCode < 0) {
        String errorMsg = (String) query.getOutputParameterValue("p_error_msg");
        throw new ShopApiException(HttpStatus.BAD_REQUEST, errorMsg);
    }
}
```
