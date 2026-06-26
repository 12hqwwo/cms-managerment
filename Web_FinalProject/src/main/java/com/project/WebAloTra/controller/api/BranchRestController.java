package com.project.WebAloTra.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.entity.Bill;
import com.project.WebAloTra.entity.Branch;
import com.project.WebAloTra.repository.BillRepository;
import com.project.WebAloTra.repository.BranchRepository;
import com.project.WebAloTra.service.BranchService;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/branches")
public class BranchRestController {

    private static final Logger logger = LoggerFactory.getLogger(BranchRestController.class);

    @Autowired
    private BranchService branchService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BillRepository billRepository;

    /**
     * API chi tiết chi nhánh - doanh thu 7 ngày + 10 hóa đơn gần nhất
     * GET /api/branches/admin/{branchId}/detail
     */
    @GetMapping("/admin/{branchId}/detail")
    public ResponseEntity<?> getBranchDetail(@PathVariable Long branchId) {
        try {
            logger.info("Getting branch detail for branchId: {}", branchId);
            
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (!branchOpt.isPresent()) {
                logger.warn("Branch not found with id: {}", branchId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Chi nhánh không tồn tại"));
            }

            Branch branch = branchOpt.get();
            Map<String, Object> response = new HashMap<>();

            // 1. Tạo dữ liệu doanh thu 7 ngày (mặc định 0 vì không có quan hệ branch-bill)
            LocalDate today = LocalDate.now();
            List<String> revenueDates = new ArrayList<>();
            List<Double> revenueValues = new ArrayList<>();
            
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                revenueDates.add(date.toString());
                revenueValues.add(0.0);  // Mặc định là 0 vì không có dữ liệu
            }

            response.put("branchId", branchId);
            response.put("branchName", branch.getBranchName());
            response.put("branchCode", branch.getBranchCode());
            response.put("revenueDates", revenueDates);
            response.put("revenueValues", revenueValues);

            logger.info("Revenue dates: {}, Revenue values: {}", revenueDates, revenueValues);

            // 2. Lấy 10 hóa đơn gần nhất (tất cả, vì không có liên kết chi nhánh)
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createDate").descending());
            List<Bill> bills = billRepository.findAll(pageable).getContent();
            
            List<Map<String, Object>> billList = new ArrayList<>();
            for (Bill bill : bills) {
                try {
                    Map<String, Object> billMap = new HashMap<>();
                    billMap.put("id", bill.getId());
                    billMap.put("code", bill.getCode() != null ? bill.getCode() : "N/A");
                    billMap.put("createDate", bill.getCreateDate() != null ? bill.getCreateDate().toLocalDate().toString() : "");
                    billMap.put("totalAmount", bill.getAmount() != null ? bill.getAmount() : 0.0);
                    billMap.put("status", bill.getStatus() != null ? bill.getStatus().toString() : "N/A");
                    billList.add(billMap);
                } catch (Exception ex) {
                    logger.error("Error processing bill: {}", bill.getId(), ex);
                }
            }

            logger.info("Found {} bills", billList.size());
            response.put("bills", billList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Lỗi lấy chi tiết chi nhánh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createBranch(@RequestBody Branch branch) {
        try {
            Branch createdBranch = branchService.createBranch(branch);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating branch: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBranchById(@PathVariable Long id) {
        Optional<Branch> branch = branchService.getBranchById(id);
        if (branch.isPresent()) {
            return ResponseEntity.ok(branch.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Branch not found");
    }

    @GetMapping("/code/{branchCode}")
    public ResponseEntity<?> getBranchByCode(@PathVariable String branchCode) {
        Optional<Branch> branch = branchService.getBranchByCode(branchCode);
        if (branch.isPresent()) {
            return ResponseEntity.ok(branch.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Branch not found");
    }

    @GetMapping
    public ResponseEntity<?> getAllBranches() {
        List<Branch> branches = branchService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveBranches() {
        List<Branch> branches = branchService.getActiveBranches();
        return ResponseEntity.ok(branches);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBranch(@PathVariable Long id, @RequestBody Branch branch) {
        try {
            Branch updatedBranch = branchService.updateBranch(id, branch);
            if (updatedBranch != null) {
                return ResponseEntity.ok(updatedBranch);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Branch not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating branch: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable Long id) {
        try {
            branchService.deleteBranch(id);
            return ResponseEntity.ok("Branch deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting branch: " + e.getMessage());
        }
    }
}