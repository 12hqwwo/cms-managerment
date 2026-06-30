# 3. Giỏ hàng (ShoppingCartController.java, CartRepository.java & VpdContextFilter.java) - Mức độ Vừa
**Kỹ thuật Oracle:** VPD Policy (Lọc theo account_id / customer_id)

**Tác động lên Java:**
VPD hoạt động ở mức Database. Để kích hoạt VPD, Java phải "báo cáo" cho Oracle biết ai đang đăng nhập bằng cách truyền `account_id` vào session context của Oracle mỗi khi có request gửi tới. Sau đó, ở các lớp Repository và Service, chúng ta không cần truyền tham số `accountId` vào các câu lệnh SQL nữa. Lệnh `findAll()` hay `deleteAll()` sẽ tự động bị Oracle thêm đuôi `WHERE account_id = ...` để chặn không cho user xem hay sửa giỏ hàng của người khác.

**Chỗ áp dụng trong Java:**

### 1. Kích hoạt VPD mỗi Request (`VpdContextFilter.java`)
Tạo một Filter (đã được cấu hình trong Spring Security) chạy trước mọi Request:
```java
// === KÍCH HOẠT VPD TỪ JAVA (VpdContextFilter.java) ===
@Component
public class VpdContextFilter extends OncePerRequestFilter {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(...) {
        // Lấy Account đang đăng nhập từ Spring Security...
        if (account != null) {
            // Báo cho Oracle biết ai đang truy vấn.
            // Hàm pkg_vpd_security.set_account_id() dưới DB sẽ gán biến vào Context.
            entityManager.createNativeQuery(
                "BEGIN TRASUA.pkg_vpd_security.set_account_id(:accountId); END;"
            ).setParameter("accountId", account.getId())
             .executeUpdate();
        }
        filterChain.doFilter(request, response);
    }
}
```

### 2. Sửa code Repository & Service (`CartRepository.java`, `CartServiceImpl.java`)
Nhờ có VPD lo liệu an ninh, code Java trở nên vô cùng ngắn gọn, sạch sẽ, không sợ hacker khai thác lỗ hổng Insecure Direct Object References (IDOR).
```java
// === CODE JAVA CŨ (Đã comment lại) ===
// List<Cart> findAllByAccount_Id(Long accountId);
// void deleteAllByAccount_Id(Long accountId);
// boolean existsByProductDetail_IdAndAccount_Id(Long pdId, Long accId);

// === CODE JAVA MỚI (Áp dụng Oracle VPD) ===
// Không cần WHERE account_id trong Java nữa! Chỉ cần lấy/xóa tất cả.
// Lệnh chạy xuống Oracle sẽ tự động bị cắt xén dữ liệu.

// Trong CartRepository.java:
List<Cart> findAll();
void deleteAll();
boolean existsByProductDetail_Id(Long productDetailId);

// Trong CartServiceImpl.java:
List<Cart> cartList = cartRepository.findAll(); // DB tự lọc ra giỏ hàng của user
cartRepository.deleteAll(); // DB tự động chỉ xóa giỏ hàng của user đó
```
