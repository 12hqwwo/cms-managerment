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
     * âœ… TĂ¬m branch_id cá»§a vendor theo email Ä‘Äƒng nháº­p
     * (Cáº§n cĂ³ quan há»‡ ManyToOne giá»¯a Account vĂ  Branch)
     */
    @Query(value = "SELECT branch_id FROM account WHERE email = :email", nativeQuery = true)
    Optional<Long> findBranchIdByEmail(@Param("email") String email);

    /**
     * âœ… Thá»‘ng kĂª sá»‘ lÆ°á»£ng tĂ i khoáº£n Ä‘Æ°á»£c táº¡o theo thĂ¡ng
     */
    @Query(value = """
            SELECT 
                TO_CHAR(a.create_date, 'MM-YYYY') AS "month",
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

    List<Account> findByBranch_Id(Long branchId);

    Account findTopByOrderByIdDesc();
}

