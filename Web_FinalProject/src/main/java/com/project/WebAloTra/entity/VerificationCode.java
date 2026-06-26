package com.project.WebAloTra.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "verification_code") // ✅ Ràng buộc tên bảng cho khớp SQL
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Khóa ngoại liên kết với Account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(length = 6, nullable = false)
    private String code;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;
}
