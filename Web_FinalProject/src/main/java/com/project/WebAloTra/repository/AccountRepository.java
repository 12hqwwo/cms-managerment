package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.WebAloTra.entity.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByEmail(String email);

    /**
     * ✅ Tìm branch_id của vendor theo email đăng nhập
     * (Cần có quan hệ ManyToOne giữa Account và Branch)
     */
    @Query(value = "SELECT branch_id FROM account WHERE email = :email", nativeQuery = true)
    Optional<Long> findBranchIdByEmail(@Param("email") String email);

    /**
     * ✅ Thống kê số lượng tài khoản được tạo theo tháng
     */
    @Query(value = """
            SELECT 
                FORMAT(a.create_date, 'MM-yyyy') AS month,
                COUNT(a.id) AS count
            FROM account a
            WHERE a.create_date BETWEEN :startDate AND :endDate
            GROUP BY FORMAT(a.create_date, 'MM-yyyy')
            ORDER BY MIN(a.create_date)
            """, nativeQuery = true)
    List<Object[]> getMonthlyAccountStatistics(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    Account findByCustomer_PhoneNumber(String phoneNumber);

    Account findTopByOrderByIdDesc();
}
