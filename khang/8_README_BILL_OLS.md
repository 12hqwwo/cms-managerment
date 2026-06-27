# 8. Hóa đơn (BillController.java & VpdContextFilter.java) - Mức độ Khó
**Kỹ thuật Oracle:** OLS (Oracle Label Security) & RBAC

**Tác động lên Java:**
- **RBAC (Spring Security):** Chặn quyền truy cập đường dẫn dựa trên Role (nhân viên, quản lý, admin).
- **OLS (Oracle Label Security):** Hoạt động bằng cách gán "Nhãn" (Label) phân cấp. Giúp bảo mật phân quyền dòng dựa trên mức độ nhạy cảm của dữ liệu (VD: Hóa đơn giá trị cao/VIP thì chỉ cấp Quản lý hoặc Giám đốc mới được thấy, dù Nhân viên có được quyền truy cập vào danh sách).

**Chỗ áp dụng trong Java:**
Thay vì phải dùng vòng lặp hay câu lệnh SQL `WHERE amount < 500000` thủ công để giấu đơn VIP đối với nhân viên thường, Java giao việc đó cho DB.

### 1. File `VpdContextFilter.java` (Gán nhãn tự động)
Mọi Request đều đi qua Filter này. Dựa vào Role của người đăng nhập, Java sẽ gửi nhãn OLS tương ứng xuống DB:
```java
// ===== ÁP DỤNG OLS (Oracle Label Security) =====
// STAFF  → Nhãn STAFF (Chỉ thấy bill < 500k)
// VENDOR → Nhãn MANAGER (Thấy bill >= 500k)
// ADMIN  → Nhãn DIRECTOR (Thấy toàn bộ)

String olsLabel = resolveOlsLabel(account);
entityManager.createNativeQuery(
    "BEGIN SA_SESSION.SET_LABEL('BILL_OLS_POL', :label); END;"
).setParameter("label", olsLabel)
 .executeUpdate();
```

### 2. File `BillController.java` (Controller siêu sạch)
Khi lấy danh sách Hóa Đơn, Controller chỉ gọi các hàm lấy danh sách bình thường (như `findAll`, `findByBranchId`). OLS dưới Database sẽ chặn lại và "tàng hình" các Hóa đơn vượt quá thẩm quyền (Label) của User hiện tại:
```java
// === CODE JAVA CŨ (Đã comment) ===
// if (isStaff) { 
//     bills = billRepository.findByAmountLessThan(500000); 
// } else { 
//     bills = billRepository.findAll(); 
// }

// === CODE JAVA MỚI (Áp dụng Oracle OLS) ===
// Dù Java gọi hàm lấy TẤT CẢ, Oracle sẽ tự động ẩn Hóa đơn VIP nếu User chỉ là STAFF!
bills = billService.findAll(pageable); 
```
