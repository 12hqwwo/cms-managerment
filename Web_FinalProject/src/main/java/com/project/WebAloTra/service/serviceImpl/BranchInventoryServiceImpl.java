package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.entity.BranchInventory;
import com.project.WebAloTra.repository.BranchInventoryRepository;
import com.project.WebAloTra.service.BranchInventoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BranchInventoryServiceImpl implements BranchInventoryService {

    @Autowired
    private BranchInventoryRepository branchInventoryRepository;

    @Override
    public BranchInventory createInventory(BranchInventory inventory) {
        inventory.setCreateDate(LocalDateTime.now());
        inventory.setUpdateDate(LocalDateTime.now());
        inventory.setActive(true);
        return branchInventoryRepository.save(inventory);
    }

    @Override
    public Optional<BranchInventory> getInventoryById(Long id) {
        return branchInventoryRepository.findById(id);
    }

    @Override
    public Optional<BranchInventory> getInventoryByBranchAndProduct(Long branchId, Long productDetailId) {
        return branchInventoryRepository.findByBranchIdAndProductDetailId(branchId, productDetailId);
    }

    @Override
    public List<BranchInventory> getInventoriesByBranchId(Long branchId) {
        return branchInventoryRepository.findByBranchId(branchId);
    }

    @Override
    public List<BranchInventory> getInventoriesByProductDetailId(Long productDetailId) {
        return branchInventoryRepository.findByProductDetailId(productDetailId);
    }

    @Override
    public List<BranchInventory> getAllInventories() {
        return branchInventoryRepository.findAll();
    }

    @Override
    public BranchInventory updateInventory(Long id, BranchInventory inventoryDetails) {
        Optional<BranchInventory> optionalInventory = branchInventoryRepository.findById(id);
        if (optionalInventory.isPresent()) {
            BranchInventory inventory = optionalInventory.get();
            if (inventoryDetails.getQuantity() != null) {
                inventory.setQuantity(inventoryDetails.getQuantity());
            }
            if (inventoryDetails.getMinQuantity() != null) {
                inventory.setMinQuantity(inventoryDetails.getMinQuantity());
            }
            if (inventoryDetails.getMaxQuantity() != null) {
                inventory.setMaxQuantity(inventoryDetails.getMaxQuantity());
            }
            inventory.setUpdateDate(LocalDateTime.now());
            return branchInventoryRepository.save(inventory);
        }
        return null;
    }

    @Override
    public void deleteInventory(Long id) {
        branchInventoryRepository.deleteById(id);
    }

    @Override
    public void updateQuantity(Long branchId, Long productDetailId, int quantity) {
        Optional<BranchInventory> optionalInventory = 
            branchInventoryRepository.findByBranchIdAndProductDetailId(branchId, productDetailId);
        if (optionalInventory.isPresent()) {
            BranchInventory inventory = optionalInventory.get();
            inventory.setQuantity(quantity);
            inventory.setUpdateDate(LocalDateTime.now());
            branchInventoryRepository.save(inventory);
        }
    }
}
