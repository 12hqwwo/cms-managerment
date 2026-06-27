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
import com.project.WebAloTra.entity.Role;
import com.project.WebAloTra.service.AccountService;
import com.project.WebAloTra.service.BranchService;
import com.project.WebAloTra.repository.RoleRepository;

import java.util.List;

@Controller
public class AccountMngController {
    private final AccountService accountService;
    private final BranchService branchService;
    private final RoleRepository roleRepository;

    public AccountMngController(AccountService accountService, BranchService branchService,
            RoleRepository roleRepository) {
        this.accountService = accountService;
        this.branchService = branchService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/admin-only/account-management")
    public String viewAccountManagementPage(Model model) {
        List<Account> accountList = accountService.findAllAccount();
        List<Branch> branchList = branchService.getAllBranches();
        List<Role> roleList = roleRepository.findAll();
        model.addAttribute("accountList", accountList);
        model.addAttribute("branchList", branchList);
        model.addAttribute("roleList", roleList);
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
    public String changeAccountRole(@ModelAttribute("email") String email,
            @ModelAttribute("role") Long roleId,
            @org.springframework.web.bind.annotation.RequestParam(value = "branchId", required = false) Long branchId,
            RedirectAttributes redirectAttributes) {
        try {
            Account account = accountService.changeRole(email, roleId, branchId);
            redirectAttributes.addFlashAttribute("message",
                    "Tài khoản " + account.getEmail() + " đã đổi quyền thành công");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
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