package com.project.WebAloTra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.Branch;
import com.project.WebAloTra.service.AccountService;
import com.project.WebAloTra.service.BranchService;

import java.util.List;

@Controller
public class AccountMngController {
    private final AccountService accountService;
    private final BranchService branchService;

    public AccountMngController(AccountService accountService, BranchService branchService) {
        this.accountService = accountService;
        this.branchService = branchService;
    }

    @GetMapping("/admin-only/account-management")
    public String viewAccountManagementPage(Model model) {
        List<Account> accountList = accountService.findAllAccount();
        model.addAttribute("accountList", accountList);
        return "/admin/account";
    }

    @PostMapping("/account/block/{id}")
    public String blockAccount(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Account account = accountService.blockAccount(id);
        redirectAttributes.addFlashAttribute("message", "Tài khoản " + account.getEmail() + " đã khóa thành công");
        return "redirect:/admin-only/account-management";
    }

    @PostMapping("/account/open/{id}")
    public String openAccount(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Account account = accountService.openAccount(id);
        redirectAttributes.addFlashAttribute("message", "Tài khoản " + account.getEmail() + " đã mở khóa thành công");
        return "redirect:/admin-only/account-management";
    }

    @PostMapping("/account/change-role")
    public String openAccount(@ModelAttribute("email") String email, @ModelAttribute("role") Long roleId, RedirectAttributes redirectAttributes) {
        Account account = accountService.changeRole(email, roleId);
        redirectAttributes.addFlashAttribute("message", "Tài khoản " + account.getEmail() + " đã đổi thành quyền thành công");
        return "redirect:/admin-only/account-management";
    }

    @GetMapping("/admin/create-vendor-account")
    public String viewCreateVendorAccountPage(Model model) {
        try {
            List<Account> availableAccounts = accountService.getVendorAccountsWithoutBranch();
            model.addAttribute("availableAccounts", availableAccounts);
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách account: " + e.getMessage());
        }
        return "/admin/create-vendor-account";
    }

    @GetMapping("/admin/branch-management")
    public String viewBranchManagementPage(Model model) {
        try {
            List<Branch> branches = branchService.getAllBranches();
            model.addAttribute("branches", branches);
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách chi nhánh: " + e.getMessage());
        }
        return "/admin/branch-management";
    }
}