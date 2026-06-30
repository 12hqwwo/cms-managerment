package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.entity.InventoryRequestDetail;

import java.util.List;

@Repository
public interface InventoryRequestDetailRepository extends JpaRepository<InventoryRequestDetail, Long> {
    List<InventoryRequestDetail> findByInventoryRequestId(Long id);
}
