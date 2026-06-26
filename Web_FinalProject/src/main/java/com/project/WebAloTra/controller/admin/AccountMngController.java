package com.project.WebAloTra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
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

    public AccountMngController(AccountService accountService, BranchService branchService, RoleRepository roleRepository) {
        this.accountService = accountService;
        this.branchService = branchService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/admin-only/account-management")
    public String viewAccountManagementPage(Model model) {
        List<Account> accountList = accountService.findAllAccount();
        List<Branch> branches = branchService.getAllBranches();
        List<Role> roles = roleRepository.findAll().stream()
                .filter(r -> !"ROLE_GUEST".equals(r.getName().name()))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("accountList", accountList);
        model.addAttribute("branches", branches);
        model.addAttribute("roles", roles);
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
    public String changeRole(@RequestParam("email") String email, @RequestParam("role") Long roleId, 
                              @RequestParam(value = "branchId", required = false) Long branchId, RedirectAttributes redirectAttributes) {
        System.out.println("====== CHANGE ROLE SUBMITTED ======");
        System.out.println("Email: " + email);
        System.out.println("RoleId: " + roleId);
        System.out.println("BranchId: " + branchId);
        Account account = accountService.changeRole(email, roleId);
        String roleName = account.getRole().getName().name();
        if (("ROLE_VENDOR".equals(roleName) || "ROLE_STAFF".equals(roleName)) && branchId != null) {
            System.out.println("Assigning branch " + branchId + " to account " + email);
            Branch branch = branchService.getBranchById(branchId).orElse(null);
            account.setBranch(branch);
        } else {
            System.out.println("Removing branch from account " + email);
            account.setBranch(null);
        }
        accountService.save(account);
        System.out.println("Role for account after save: " + account.getRole().getName());
        redirectAttributes.addFlashAttribute("message", "Cập nhật tài khoản thành công!");
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