package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.entity.Branch;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByBranchCode(String branchCode);
    Optional<Branch> findByBranchName(String branchName);

    /**
     * Lấy tất cả chi nhánh đang hoạt động
     */
    List<Branch> findByIsActiveTrue();

    /**
     * Tìm chi nhánh theo email
     */
    Optional<Branch> findByEmail(String email);
}