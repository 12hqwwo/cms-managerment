package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.VerificationCode;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByAccount(Account account);
    Optional<VerificationCode> findByCodeAndAccount(String code, Account account);
    Optional<VerificationCode> findByCode(String code); // ✅ giữ lại dòng này cho verifyCode()
}