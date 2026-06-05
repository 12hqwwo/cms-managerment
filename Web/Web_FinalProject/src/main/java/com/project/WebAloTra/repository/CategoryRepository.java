package com.project.WebAloTra.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.WebAloTra.entity.Category;
import com.project.WebAloTra.entity.Size;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCode(String code);
    List<Category> findAllByDeleteFlagFalse();
    Page<Category> findAllByDeleteFlagFalse(Pageable pageable);
    
}