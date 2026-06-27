package com.project.WebAloTra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.WebAloTra.dto.CustomerDto.CustomerDto;
import com.project.WebAloTra.dto.Statistic.TopCustomerBuy;
import com.project.WebAloTra.entity.Customer;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCode(String code);



    Customer findTopByOrderByIdDesc();

    Customer findByCode(String code);

    @Query("SELECT c FROM Customer c WHERE c.code LIKE %:keyword% OR c.name LIKE %:keyword% OR c.phoneNumber LIKE %:keyword%")
    Page<Customer> searchCustomerKeyword(String keyword,Pageable pageable);

//    @Query("SELECT distinct c from Customer c join Bill b on c.id = b.customer.id join BillDetail bd on b.id = bd.id where bd.id = :billDetailId")
//    Customer findByBillDetailId(@Param("billDetailId") Long billDetailId);
//
//    @Query("SELECT distinct c from Customer c join Bill b on c.id = b.customer.id join BillDetail bd on b.id = bd.id join ReturnDetail rd on bd.id = rd.billDetail.id join BillReturn br on br.id = rd.billReturn.id where bd.id = :billReturnId")
//    Customer findByBillBillReturnId(@Param("billReturnId") Long billReturnId);

    @Query(value = "SELECT * FROM (" +
            "SELECT c.code, c.name, COUNT(c.id) AS totalPurchases, SUM(b.amount) AS revenue " +
            "FROM customer c " +
            "JOIN bill b ON b.customer_id = c.id " +
            "JOIN bill_detail bd ON b.id = bd.bill_id " +
            "GROUP BY c.id, c.name, c.code " +
            "ORDER BY COUNT(c.id) DESC" +
            ") WHERE ROWNUM <= 5", nativeQuery = true)
    List<TopCustomerBuy> findTopCustomersByPurchases();

    boolean existsByPhoneNumber(String phoneNumber);

    Customer findByPhoneNumber(String phoneNumber);

    Customer findByAccount_Id(Long id);
    Customer findByAccount_Email(String email);
}
