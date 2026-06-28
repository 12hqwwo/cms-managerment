# 8. Hóa đơn (BillController.java) - Mức độ Khó
**Kỹ thuật Oracle:** OLS (Oracle Label Security) & RBAC

**Tác động lên Java:**
RBAC chặn quyền truy cập đường dẫn (chỉ nhân viên/quản trị được vào xem danh sách).
OLS hoạt động bằng cách gán "Nhãn" (Label) phân cấp. Java phải gán nhãn này cho Session Oracle khi user lấy danh sách hóa đơn (tương tự VPD nhưng là phân quyền dòng dựa trên mức độ nhạy cảm của dữ liệu, VD: Hóa đơn >= 500k thì chỉ Admin mới được thấy).

**Chỗ áp dụng trong Java:**
Mở `BillController.java`, hàm lấy danh sách hóa đơn quản trị (`getAllBill`):
```java
// === CODE JAVA CŨ ===
// List<Bill> bills;
// if (user.getRole().equals("ROLE_STAFF")) { 
//     bills = billRepository.findByAmountLessThan(500000); 
// } else { 
//     bills = billRepository.findAll(); 
// }

// === CODE JAVA MỚI (Áp dụng Oracle OLS) ===
@Autowired private JdbcTemplate jdbcTemplate;

@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
@GetMapping("/admin/bills")
public String getAllBills(Model model) {
    String role = accountService.getAccountLogin().getRole().getName();
    String oracleLabel = "CUSTOMER"; // Mặc định nhãn thấp nhất (dành cho ROLE_USER)
    
    if (role.equals("ROLE_ADMIN")) {
        oracleLabel = "ADMIN"; // Nhãn cao nhất (Thấy hết)
    } else if (role.equals("ROLE_STAFF")) {
        oracleLabel = "STAFF"; // Nhãn nhân viên (Chỉ thấy < 500k)
    } else if (role.equals("ROLE_VENDOR")) {
        oracleLabel = "VENDOR"; // Nhãn đối tác
    }

    // Gán Label cho kết nối hiện tại để Oracle OLS kích hoạt
    jdbcTemplate.execute("BEGIN SA_SESSION.SET_LABEL('BILL_OLS_POL', '" + oracleLabel + "'); END;");
    
    // Java gọi lấy TẤT CẢ hóa đơn, nhưng Oracle sẽ tự giấu Hóa đơn >= 500k nếu User chỉ có nhãn STAFF!
    List<Bill> bills = billRepository.findAll();
    model.addAttribute("bills", bills);
    return "admin/bill";
}
```
