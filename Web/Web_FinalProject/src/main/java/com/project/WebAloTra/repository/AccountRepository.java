package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
                TO_CHAR(a.create_date, 'MM-YYYY') AS month,
                COUNT(a.id) AS count
            FROM account a
            WHERE a.create_date BETWEEN TO_TIMESTAMP(:startDate, 'YYYY-MM-DD') AND TO_TIMESTAMP(:endDate, 'YYYY-MM-DD')
            GROUP BY TO_CHAR(a.create_date, 'MM-YYYY')
            ORDER BY MIN(a.create_date)
            """, nativeQuery = true)
    List<Object[]> getMonthlyAccountStatistics(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    Account findByCustomer_PhoneNumber(String phoneNumber);

    Account findTopByOrderByIdDesc();

    /**
     * Gọi Stored Procedure PROC_CHANGE_PASSWORD để đổi mật khẩu và lưu lịch sử
     */
    @Transactional
    @Modifying
    @Query(value = "CALL PROC_CHANGE_PASSWORD(:accountId, :oldPassword, :newPassword, :changedBy)", nativeQuery = true)
    void changePassword(
            @Param("accountId") Long accountId,
            @Param("oldPassword") String oldPassword,
            @Param("newPassword") String newPassword,
            @Param("changedBy") String changedBy
    );
}
