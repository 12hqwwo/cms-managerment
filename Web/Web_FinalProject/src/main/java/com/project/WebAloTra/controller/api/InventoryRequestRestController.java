package com.project.WebAloTra.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.InventoryRequest.InventoryRequestDto;
import com.project.WebAloTra.entity.InventoryRequest;
import com.project.WebAloTra.service.InventoryRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/vendor/inventory-request")
public class InventoryRequestRestController {

    @Autowired
    private InventoryRequestService inventoryRequestService;

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody InventoryRequestDto requestDto, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String email = authentication.getName();
        try {
            inventoryRequestService.createRequest(requestDto, email);
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("message", "Success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
    private com.project.WebAloTra.repository.ProductDetailRepository productDetailRepository;

    @GetMapping("/master-products")
    public ResponseEntity<?> getMasterProducts() {
        // Fetch all product details, filtering only active products
        // For simplicity, we just fetch all and map to a simple structure
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        
        productDetailRepository.findAll().forEach(pd -> {
            if (pd.getProduct().getStatus() == 1 && !pd.getProduct().isDeleteFlag()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("productDetailId", pd.getId());
                map.put("productCode", pd.getProduct().getCode());
                map.put("productName", pd.getProduct().getName());
                map.put("size", pd.getSize() != null ? pd.getSize().getName() : "");
                map.put("color", pd.getColor() != null ? pd.getColor().getName() : ""); // sugar/ice level is often stored here in milk tea apps
                result.add(map);
            }
        });
        
        return ResponseEntity.ok(result);
    }
}
