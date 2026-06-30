# 🔗 Ánh xạ SQL - Java (Oracle Security)

Dưới đây là bảng tóm tắt các tính năng bảo mật Oracle tương ứng với các file Java chịu ảnh hưởng trong hệ thống:

| File SQL | Áp dụng vào File Java | Chức năng / Tác động |
|----------|-----------------------|----------------------|
| `1_PROC_CREATE_ORDER.sql` | `CartServiceImpl.java`<br>`OrderController.java` | Gọi DB Procedure thay vì Code Java để quản lý transaction thêm đơn/trừ kho. |
| `2_PROC_PAYMENT.sql` | `PaymentController.java`<br>`PaymentRestController.java` | Gọi Procedure xác nhận thanh toán VNPay trực tiếp trong DB. |
| `3_VPD_CART.sql` | `ShoppingCartController.java`<br>`VpdContextFilter.java` | Tự động lọc giỏ hàng theo User đang đăng nhập (Bỏ lệnh `WHERE` trong Java). |
| `4_VPD_ADDRESS.sql` | `AddressShippingController.java`| Tự động lọc địa chỉ giao hàng theo User. |
| `5_VPD_ORDERSTATUS.sql` | `OrderStatusController.java` | Tự động lọc danh sách đơn mua theo User. |
| `6_FGA_REFUND.sql` | `RefundController.java` | Tự động ghi log thao tác Hoàn tiền xuống DB. |
| `7_FGA_BILLRETURN.sql` | `BillReturnController.java` | Tự động ghi log thao tác thêm/sửa Phiếu trả hàng xuống DB. |
| `8_OLS_BILL.sql` | `BillController.java`<br>`VpdContextFilter.java` | Phân quyền hóa đơn VIP theo cấp bậc (Chỉ gọi `findAll` trong Java). |
| `9_PROC_UPDATE_BILL_STATUS.sql`| `BillServiceImpl.java` | Cập nhật trạng thái đơn hàng bằng Procedure. |

> **⚠️ Ghi chú chung:**
> Tất cả các cơ chế bảo mật cấp dòng (VPD, OLS) đều phụ thuộc vào file `VpdContextFilter.java`. File này chạy ngầm để thiết lập Session Context cho Oracle tại mỗi Request trước khi Controller/Service được gọi.