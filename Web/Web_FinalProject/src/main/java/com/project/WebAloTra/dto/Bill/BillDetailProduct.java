package com.project.WebAloTra.dto.Bill;

public interface BillDetailProduct {

    Long getBillDetailId();

    String getTenSanPham();

    String getTenMau();

    String getKichCo();

    Double getGiaTien();     // ✅ đây là giá gốc trà sữa (moment_price)

    Integer getSoLuong();
    

    Double getTongTopping(); // ✅ tổng topping của dòng, không cần đổi tên
    String getTenTopping();

    Double getTongTien();    // ✅ moment_price * quantity

    String getImageUrl();

    Long getProductDetailId();
}
