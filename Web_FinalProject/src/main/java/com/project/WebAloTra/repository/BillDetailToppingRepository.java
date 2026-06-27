package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.entity.BillDetailTopping;

import java.util.List;

@Repository
public interface BillDetailToppingRepository extends JpaRepository<BillDetailTopping, Long> {

    /**
     * Lấy tất cả topping thuộc 1 bill_detail cụ thể
     * (để cộng vào tổng tiền)
     */
    List<BillDetailTopping> findAllByBillDetail_Id(Long billDetailId);
}
