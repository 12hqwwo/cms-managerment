package com.project.WebAloTra.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.WebAloTra.dto.Account.AssignBranchRequest;
import com.project.WebAloTra.dto.Account.AssignBranchWithInfoRequest;
import com.project.WebAloTra.dto.Account.CreateVendorAccountRequest;
import com.project.WebAloTra.dto.Response.ErrorResponse;
import com.project.WebAloTra.dto.Response.SuccessResponse;
import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.Branch;
import com.project.WebAloTra.service.AccountBranchService;

import java.util.*;

@RestController
@RequestMapping("/api/admin/accounts-branches")
public class AdminAccountBranchController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountBranchController.class);

    @Autowired
    private AccountBranchService accountBranchService;

    // -------------------- TẠO MỚI VENDOR ACCOUNT --------------------
    @PostMapping("/create-vendor-with-branch")
    public ResponseEntity<?> createVendorAccountWithBranch(@RequestBody CreateVendorAccountRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("Email không được để trống");
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body("Mật khẩu không được để trống");
            }
            if (request.getBranchCode() == null || request.getBranchCode().isEmpty()) {
                return ResponseEntity.badRequest().body("Mã chi nhánh không được để trống");
            }

            Account createdAccount = accountBranchService.createVendorAccountWithBranch(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
        } catch (Exception e) {
            logger.error("Lỗi tạo vendor account", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    // -------------------- GÁN BRANCH CHO ACCOUNT HIỆN CÓ --------------------
    @PostMapping("/assign-branch-with-info")
    public ResponseEntity<?> assignBranchWithInfo(@RequestBody AssignBranchWithInfoRequest request) {
        try {
            if (request.getAccountId() == null)
                return ResponseEntity.badRequest().body(new ErrorResponse("Account ID không được để trống"));
            if (request.getBranchCode() == null || request.getBranchCode().isEmpty())
                return ResponseEntity.badRequest().body(new ErrorResponse("Mã chi nhánh không được để trống"));

            Branch branch = new Branch();
            branch.setBranchCode(request.getBranchCode());
            branch.setBranchName(request.getBranchName());
            branch.setAddress(request.getBranchAddress());
            branch.setPhone(request.getBranchPhone());
            branch.setEmail(request.getBranchEmail());

            Account updatedAccount = accountBranchService.assignBranchWithInfo(request.getAccountId(), branch);
            return ResponseEntity.ok(new SuccessResponse("Gán branch thành công", null));
        } catch (Exception e) {
            logger.error("Lỗi gán branch cho account", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Lỗi gán branch: " + e.getMessage()));
        }
    }

    // -------------------- LẤY DANH SÁCH ACCOUNT CHƯA CÓ BRANCH --------------------
    @GetMapping("/available-accounts")
    public ResponseEntity<?> getAvailableAccounts() {
        try {
            List<Account> accounts = accountBranchService.getAccountsWithoutBranch();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    // -------------------- LẤY VENDOR ACCOUNT THEO BRANCH --------------------
    @GetMapping("/vendor-by-branch/{branchId}")
    public ResponseEntity<?> getVendorAccountByBranch(@PathVariable Long branchId) {
        try {
            Optional<Account> account = accountBranchService.getVendorAccountByBranchId(branchId);
            return account.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Không tìm thấy vendor account cho branch này"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    // -------------------- XÓA BRANCH KHỎI ACCOUNT --------------------
    @DeleteMapping("/remove-branch/{accountId}")
    public ResponseEntity<?> removeBranchFromAccount(@PathVariable Long accountId) {
        try {
            Account updatedAccount = accountBranchService.removeBranchFromAccount(accountId);
            if (updatedAccount != null) {
                return ResponseEntity.ok("Xóa branch khỏi account thành công");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account không tồn tại");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi xóa branch: " + e.getMessage());
        }
    }

    // -------------------- THỐNG KÊ DOANH THU CHI NHÁNH --------------------
    @GetMapping("/statistics")
    public ResponseEntity<?> getBranchStatistics() {
        try {
            List<Map<String, Object>> stats = accountBranchService.getBranchRevenueStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Lỗi khi thống kê doanh thu chi nhánh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
