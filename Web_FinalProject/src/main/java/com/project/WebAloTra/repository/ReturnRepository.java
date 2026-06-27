package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.WebAloTra.entity.BillReturn;

public interface ReturnRepository extends JpaRepository<BillReturn, Long> {
}
