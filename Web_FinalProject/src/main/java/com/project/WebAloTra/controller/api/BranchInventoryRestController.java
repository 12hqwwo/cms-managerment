package com.project.WebAloTra.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.entity.BranchInventory;
import com.project.WebAloTra.service.BranchInventoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/branch-inventories")
public class BranchInventoryRestController {

    @Autowired
    private BranchInventoryService branchInventoryService;

    @PostMapping
    public ResponseEntity<?> createInventory(@RequestBody BranchInventory inventory) {
        try {
            BranchInventory createdInventory = branchInventoryService.createInventory(inventory);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating inventory: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryById(@PathVariable Long id) {
        Optional<BranchInventory> inventory = branchInventoryService.getInventoryById(id);
        if (inventory.isPresent()) {
            return ResponseEntity.ok(inventory.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventory not found");
    }

    @GetMapping("/branch/{branchId}/product/{productDetailId}")
    public ResponseEntity<?> getInventoryByBranchAndProduct(@PathVariable Long branchId, @PathVariable Long productDetailId) {
        Optional<BranchInventory> inventory = branchInventoryService.getInventoryByBranchAndProduct(branchId, productDetailId);
        if (inventory.isPresent()) {
            return ResponseEntity.ok(inventory.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventory not found");
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getInventoriesByBranch(@PathVariable Long branchId) {
        List<BranchInventory> inventories = branchInventoryService.getInventoriesByBranchId(branchId);
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/product/{productDetailId}")
    public ResponseEntity<?> getInventoriesByProduct(@PathVariable Long productDetailId) {
        List<BranchInventory> inventories = branchInventoryService.getInventoriesByProductDetailId(productDetailId);
        return ResponseEntity.ok(inventories);
    }

    @GetMapping
    public ResponseEntity<?> getAllInventories() {
        List<BranchInventory> inventories = branchInventoryService.getAllInventories();
        return ResponseEntity.ok(inventories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInventory(@PathVariable Long id, @RequestBody BranchInventory inventory) {
        try {
            BranchInventory updatedInventory = branchInventoryService.updateInventory(id, inventory);
            if (updatedInventory != null) {
                return ResponseEntity.ok(updatedInventory);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventory not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating inventory: " + e.getMessage());
        }
    }

    @PutMapping("/branch/{branchId}/product/{productDetailId}/quantity/{quantity}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long branchId, @PathVariable Long productDetailId, @PathVariable int quantity) {
        try {
            branchInventoryService.updateQuantity(branchId, productDetailId, quantity);
            return ResponseEntity.ok("Quantity updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating quantity: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable Long id) {
        try {
            branchInventoryService.deleteInventory(id);
            return ResponseEntity.ok("Inventory deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting inventory: " + e.getMessage());
        }
    }
}