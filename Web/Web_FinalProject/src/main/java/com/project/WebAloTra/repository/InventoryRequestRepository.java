package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.WebAloTra.entity.InventoryRequest;

import java.util.List;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

@Repository
public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Long> {
    List<InventoryRequest> findByBranchIdOrderByCreatedAtDesc(Long branchId);
    
    // Add sorting for all requests (admin view)
    List<InventoryRequest> findAllByOrderByCreatedAtDesc();

    @Procedure(procedureName = "PROC_INVENTORY_REQUISITION", outputParameterName = "p_error_msg")
    String callApproveProcedure(
        @Param("p_request_id") Long requestId, 
        @Param("p_status") String status
    );
}
