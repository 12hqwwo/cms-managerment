package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.WebAloTra.entity.PaymentMethod;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}