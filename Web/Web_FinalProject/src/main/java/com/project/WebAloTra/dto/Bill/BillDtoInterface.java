package com.project.WebAloTra.dto.Bill;

import java.time.LocalDateTime;
import java.util.Date;

import com.project.WebAloTra.entity.enumClass.BillStatus;
import com.project.WebAloTra.entity.enumClass.InvoiceType;

public interface BillDtoInterface {
    Long getMaHoaDon();
    String getMaDinhDanh();
    String getHoVaTen();
    String getSoDienThoai();
    LocalDateTime getNgayTao();
    Double getTongTien();
    BillStatus getTrangThai();
    InvoiceType getLoaiDon();
    String getHinhThucThanhToan();
    String getNhanVienThanhToan();

    String getMaGiaoDich();
    String getMaDoiTra();
}
