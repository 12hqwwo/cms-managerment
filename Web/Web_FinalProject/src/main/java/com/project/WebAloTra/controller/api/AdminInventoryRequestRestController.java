package com.project.WebAloTra.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import com.project.WebAloTra.entity.InventoryRequest;
import com.project.WebAloTra.entity.InventoryRequestDetail;
import com.project.WebAloTra.repository.InventoryRequestRepository;
import com.project.WebAloTra.repository.InventoryRequestDetailRepository;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import javax.persistence.EntityManager;

@RestController
@RequestMapping("/api/admin/inventory-requests")
public class AdminInventoryRequestRestController {

    @Autowired
    private InventoryRequestRepository requestRepository;

    @Autowired
    private InventoryRequestDetailRepository requestDetailRepository;

    @Autowired
    private EntityManager entityManager;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllRequests() {
        List<InventoryRequest> requests = requestRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (InventoryRequest req : requests) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", req.getId());
            map.put("branchName", req.getBranch().getBranchName());
            map.put("createdByEmail", req.getCreatedBy() != null ? req.getCreatedBy().getEmail() : "");
            map.put("createdAt", req.getCreatedAt());
            map.put("status", req.getStatus().name());
            map.put("note", req.getNote());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRequestDetails(@PathVariable Long id) {
        InventoryRequest request = requestRepository.findById(id).orElse(null);
        if (request == null) return ResponseEntity.notFound().build();

        List<InventoryRequestDetail> details = requestDetailRepository.findByInventoryRequestId(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", request.getId());
        response.put("status", request.getStatus().name());
        
        List<Map<String, Object>> items = new ArrayList<>();
        for (InventoryRequestDetail d : details) {
            Map<String, Object> item = new HashMap<>();
            item.put("detailId", d.getId());
            item.put("productCode", d.getProductDetail().getProduct().getCode());
            item.put("productName", d.getProductDetail().getProduct().getName());
            item.put("size", d.getProductDetail().getSize() != null ? d.getProductDetail().getSize().getName() : "");
            item.put("color", d.getProductDetail().getColor() != null ? d.getProductDetail().getColor().getName() : "");
            item.put("masterInventoryQty", d.getProductDetail().getQuantity()); // Total available in system
            item.put("requestedQuantity", d.getRequestedQuantity());
            items.add(item);
        }
        response.put("details", items);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/approve")
    @Transactional
    public ResponseEntity<?> approveRequest(@PathVariable Long id, @RequestBody List<Map<String, Object>> approveData) {
        boolean isPartial = false;
        
        // Update the approved quantities first
        for (Map<String, Object> data : approveData) {
            Long detailId = Long.valueOf(data.get("detailId").toString());
            Integer approvedQty = Integer.valueOf(data.get("approvedQuantity").toString());
            
            InventoryRequestDetail detail = requestDetailRepository.findById(detailId).orElse(null);
            if (detail != null && detail.getInventoryRequest().getId().equals(id)) {
                detail.setApprovedQuantity(approvedQty);
                requestDetailRepository.save(detail);
                
                if (approvedQty < detail.getRequestedQuantity()) {
                    isPartial = true;
                }
            }
        }
        
        // Cực kỳ quan trọng: Phải flush để JPA đẩy dữ liệu UPDATE approved_quantity xuống DB
        // trước khi gọi Oracle Procedure, nếu không Procedure sẽ đọc approved_quantity = 0 (giá trị cũ)
        requestDetailRepository.flush();
        
        // Cực kỳ quan trọng: OLS Security
        // Admin chỉ có quyền CONFIDENTIAL:CN1,CN2... nhưng khi insert/update branch_inventory
        // của 1 chi nhánh cụ thể, OLS yêu cầu quyền WRITE_DOWN nếu nhãn của row thấp hơn nhãn Admin.
        // Để không bị lỗi ORA-12432, ta chủ động đổi nhãn session của Admin bằng đúng nhãn chi nhánh đó.
        InventoryRequest req = requestRepository.findById(id).orElse(null);
        if (req != null) {
            Long branchId = req.getBranch().getId();
            try {
                entityManager.createNativeQuery("BEGIN SA_SESSION.SET_LABEL('BRANCH_OLS_POLICY', 'CONFIDENTIAL:CN" + branchId + "'); END;").executeUpdate();
            } catch (Exception e) {
                // Bỏ qua nếu OLS chưa được kích hoạt
            }
        }
        
        // Call Oracle Procedure
        String status = isPartial ? "PARTIAL_APPROVED" : "APPROVED"; 
        String result = requestRepository.callApproveProcedure(id, status);
        
        if (!"SUCCESS".equals(result)) {
            return ResponseEntity.badRequest().body("Lỗi từ DB: " + result);
        }
        
        return ResponseEntity.ok(Map.of("message", "Success"));
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public ResponseEntity<?> rejectRequest(@PathVariable Long id) {
        String status = "REJECTED";
        String result = requestRepository.callApproveProcedure(id, status);
        if (!"SUCCESS".equals(result)) {
            return ResponseEntity.badRequest().body("Lỗi từ DB: " + result);
        }
        return ResponseEntity.ok(Map.of("message", "Success"));
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<?> handleDataAccessException(org.springframework.dao.DataAccessException e) {
        e.printStackTrace(); // In ra console để biết chi tiết lỗi (Root Cause)
        
        String errorMsg = e.getMessage();
        if (errorMsg != null && errorMsg.contains("ORA-02290")) {
            return ResponseEntity.badRequest().body("Không đủ hàng trong kho tổng để duyệt yêu cầu này!");
        } else if (errorMsg != null && errorMsg.contains("PLS-00306")) {
            return ResponseEntity.badRequest().body("Lỗi tham số DB Procedure. Vui lòng liên hệ Admin.");
        }
        
        return ResponseEntity.status(500).body("Lỗi hệ thống CSDL: " + e.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Lỗi máy chủ: " + e.getMessage());
    }
}
