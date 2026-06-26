package com.project.WebAloTra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.WebAloTra.entity.Material;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    boolean existsByCode(String code);
    List<Material> findAllByDeleteFlagFalse();

    Page<Material> findAllByDeleteFlagFalse(Pageable pageable);
}