package com.project.WebAloTra.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.entity.BranchInventory;
import com.project.WebAloTra.repository.BranchInventoryRepository;
import com.project.WebAloTra.repository.BranchRepository;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/branch-products")
public class BranchProductController {

    private static final Logger logger = LoggerFactory.getLogger(BranchProductController.class);

    @Autowired
    private BranchInventoryRepository branchInventoryRepository;

    @Autowired
    private BranchRepository branchRepository;

    /**
     * Lấy danh sách tất cả chi nhánh
     * GET /api/branch-products/branches
     */
    @GetMapping("/branches")
    public ResponseEntity<?> getAllBranches() {
        try {
            List<Map<String, Object>> branches = branchRepository.findByIsActiveTrue()
                    .stream()
                    .map(branch -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", branch.getId());
                        map.put("code", branch.getBranchCode());
                        map.put("branchName", branch.getBranchName());
                        map.put("address", branch.getAddress());
                        map.put("phone", branch.getPhone());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(branches);
        } catch (Exception e) {
            logger.error("Error getting branches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sản phẩm còn hàng của chi nhánh
     * GET /api/branch-products/branch/{branchId}
     */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getProductsByBranch(@PathVariable Long branchId) {
        try {
            logger.info("Getting products for branch: {}", branchId);

            // Kiểm tra chi nhánh có tồn tại không
            if (!branchRepository.existsById(branchId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Chi nhánh không tồn tại"));
            }

            List<BranchInventory> inventories = branchInventoryRepository.findActiveProductsByBranch(branchId);

            List<Map<String, Object>> products = inventories.stream()
                    .map(inventory -> {
                        Map<String, Object> product = new HashMap<>();
                        product.put("inventoryId", inventory.getId());
                        product.put("productDetailId", inventory.getProductDetail().getId());
                        product.put("productId", inventory.getProductDetail().getProduct().getId());
                        product.put("productCode", inventory.getProductDetail().getProduct().getCode());
                        product.put("productName", inventory.getProductDetail().getProduct().getName());
                        product.put("price", inventory.getProductDetail().getProduct().getPrice());
                        product.put("color", inventory.getProductDetail().getColor());
                        product.put("size", inventory.getProductDetail().getSize());
                        product.put("quantity", inventory.getQuantity());
                        product.put("describe", inventory.getProductDetail().getProduct().getDescribe());
                        return product;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "branchId", branchId,
                    "totalProducts", products.size(),
                    "products", products
            ));

        } catch (Exception e) {
            logger.error("Error getting products by branch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * Lấy sản phẩm theo danh mục của chi nhánh
     * GET /api/branch-products/branch/{branchId}/category/{categoryId}
     */
    @GetMapping("/branch/{branchId}/category/{categoryId}")
    public ResponseEntity<?> getProductsByBranchAndCategory(
            @PathVariable Long branchId,
            @PathVariable Long categoryId) {
        try {
            logger.info("Getting products for branch: {}, category: {}", branchId, categoryId);

            List<BranchInventory> inventories = branchInventoryRepository
                    .findProductsByBranchAndCategory(branchId, categoryId);

            List<Map<String, Object>> products = inventories.stream()
                    .map(inventory -> {
                        Map<String, Object> product = new HashMap<>();
                        product.put("inventoryId", inventory.getId());
                        product.put("productDetailId", inventory.getProductDetail().getId());
                        product.put("productId", inventory.getProductDetail().getProduct().getId());
                        product.put("productName", inventory.getProductDetail().getProduct().getName());
                        product.put("price", inventory.getProductDetail().getProduct().getPrice());
                        product.put("color", inventory.getProductDetail().getColor());
                        product.put("size", inventory.getProductDetail().getSize());
                        product.put("quantity", inventory.getQuantity());
                        return product;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "branchId", branchId,
                    "categoryId", categoryId,
                    "totalProducts", products.size(),
                    "products", products
            ));

        } catch (Exception e) {
            logger.error("Error getting products by branch and category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra sản phẩm có còn hàng ở chi nhánh không
     * GET /api/branch-products/check/{branchId}/{productDetailId}
     */
    @GetMapping("/check/{branchId}/{productDetailId}")
    public ResponseEntity<?> checkProductInStock(
            @PathVariable Long branchId,
            @PathVariable Long productDetailId) {
        try {
            Integer quantity = branchInventoryRepository
                    .getQuantityByBranchAndProductDetail(branchId, productDetailId);

            boolean inStock = quantity != null && quantity > 0;

            return ResponseEntity.ok(Map.of(
                    "branchId", branchId,
                    "productDetailId", productDetailId,
                    "inStock", inStock,
                    "quantity", quantity != null ? quantity : 0
            ));

        } catch (Exception e) {
            logger.error("Error checking product stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
}
