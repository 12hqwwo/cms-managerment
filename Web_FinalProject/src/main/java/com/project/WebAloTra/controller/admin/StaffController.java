package com.project.WebAloTra.controller.admin;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.Bill;
import com.project.WebAloTra.entity.enumClass.BillStatus;
import com.project.WebAloTra.repository.AccountRepository;
import com.project.WebAloTra.repository.BillRepository;
import com.project.WebAloTra.service.AccountService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final BillRepository billRepository;

    public StaffController(AccountService accountService,
                           AccountRepository accountRepository,
                           BillRepository billRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.billRepository = billRepository;
    }

    /**
     * Dashboard dành riêng cho STAFF.
     * Hiển thị thống kê đơn hàng của chi nhánh được gán trong ngày hôm nay theo 3 nhóm trạng thái:
     *  - Đơn mới      : CHO_XAC_NHAN
     *  - Đang xử lý  : CHO_LAY_HANG + CHO_GIAO_HANG
     *  - Hoàn thành  : HOAN_THANH
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/dashboard")
    public String staffDashboard(Authentication authentication, Model model) {

        // ── 1. Lấy tài khoản đang đăng nhập ──────────────────────────────────
        String email = authentication.getName();
        Account account = accountService.findByEmail(email);

        if (account == null) {
            model.addAttribute("error", "Không tìm thấy tài khoản!");
            return "error";
        }

        if (account.getBranch() == null) {
            model.addAttribute("error", "Tài khoản chưa được gán chi nhánh. Vui lòng liên hệ Admin.");
            return "error";
        }

        Long branchId = account.getBranch().getId();
        String branchName = account.getBranch().getBranchName();

        // ── 2. Khoảng thời gian ngày hôm nay ────────────────────────────────
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(LocalTime.MAX);

        // ── 3. Lấy toàn bộ bill của chi nhánh trong ngày ────────────────────
        // Dùng findAll() + stream filter để tránh thêm query mới vào repository
        List<Bill> allBillsToday = billRepository.findAll().stream()
                .filter(b -> b.getBranch() != null
                        && b.getBranch().getId().equals(branchId)
                        && b.getCreateDate() != null
                        && !b.getCreateDate().isBefore(startOfDay)
                        && !b.getCreateDate().isAfter(endOfDay))
                .toList();

        // ── 4. Đếm theo nhóm trạng thái ─────────────────────────────────────
        long countNew = allBillsToday.stream()
                .filter(b -> BillStatus.CHO_XAC_NHAN.equals(b.getStatus()))
                .count();

        long countProcessing = allBillsToday.stream()
                .filter(b -> BillStatus.CHO_LAY_HANG.equals(b.getStatus())
                          || BillStatus.CHO_GIAO_HANG.equals(b.getStatus()))
                .count();

        long countCompleted = allBillsToday.stream()
                .filter(b -> BillStatus.HOAN_THANH.equals(b.getStatus()))
                .count();

        // ── 5. Đưa dữ liệu ra view ───────────────────────────────────────────
        model.addAttribute("branchId",       branchId);
        model.addAttribute("branchName",     branchName);
        model.addAttribute("countNew",        countNew);
        model.addAttribute("countProcessing", countProcessing);
        model.addAttribute("countCompleted",  countCompleted);
        model.addAttribute("totalToday",      allBillsToday.size());

        return "staff/staff-dashboard";
    }
}
