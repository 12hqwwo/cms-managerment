package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.project.WebAloTra.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(String orderId);

    Payment findByOrderId(String orderId);

    // ✅ Cập nhật trực tiếp (dành cho cả Guest)
    @Modifying
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    @Query(value = """
        UPDATE payment 
        SET bill_id = :billId, 
            orderStatus = '1', 
            paymentDate = SYSDATE
        WHERE id = :paymentId
        """, nativeQuery = true)
    void updateBillAndStatus(@Param("billId") Long billId, @Param("paymentId") Long paymentId);
}