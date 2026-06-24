package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.WebAloTra.dto.Account.CreateVendorAccountRequest;
import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.Branch;
import com.project.WebAloTra.entity.Role;
import com.project.WebAloTra.entity.enumClass.RoleName;
import com.project.WebAloTra.repository.AccountRepository;
import com.project.WebAloTra.repository.BranchRepository;
import com.project.WebAloTra.repository.RoleRepository;
import com.project.WebAloTra.service.AccountBranchService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AccountBranchServiceImpl implements AccountBranchService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    // -------------------- GÁN BRANCH CHO ACCOUNT --------------------
    @Override
    @Transactional
    public Account assignBranchToAccount(Long accountId, Long branchId) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);

        if (optionalAccount.isPresent() && optionalBranch.isPresent()) {
            Account account = optionalAccount.get();
            Branch branch = optionalBranch.get();

            // Liên kết 2 chiều
            account.setBranch(branch);
            branch.setVendorAccount(account);

            account.setUpdateDate(LocalDateTime.now());
            branch.setUpdateDate(LocalDateTime.now());

            branchRepository.save(branch);
            return accountRepository.save(account);
        }
        throw new RuntimeException("Không tìm thấy account hoặc branch tương ứng.");
    }

    // -------------------- TẠO MỚI VENDOR ACCOUNT KÈM BRANCH --------------------
    @Override
    @Transactional
    public Account createVendorAccountWithBranch(CreateVendorAccountRequest request) {
        Branch branch = new Branch();
        branch.setBranchCode(request.getBranchCode());
        branch.setBranchName(request.getBranchName());
        branch.setAddress(request.getBranchAddress());
        branch.setPhone(request.getBranchPhone());
        branch.setEmail(request.getBranchEmail());
        branch.setActive(true);
        branch.setCreateDate(LocalDateTime.now());
        branch.setUpdateDate(LocalDateTime.now());

        Branch savedBranch = branchRepository.save(branch);

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setCode(request.getCode());
        account.setBranch(savedBranch);
        account.setNonLocked(true);
        account.setCreateDate(LocalDateTime.now());
        account.setUpdateDate(LocalDateTime.now());

        Optional<Role> vendorRole = roleRepository.findByName(RoleName.ROLE_VENDOR);
        vendorRole.ifPresent(account::setRole);

        savedBranch.setVendorAccount(account);

        accountRepository.save(account);
        branchRepository.save(savedBranch);

        return account;
    }

    // -------------------- LẤY ACCOUNT THEO BRANCH --------------------
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getVendorAccountByBranchId(Long branchId) {
        return accountRepository.findAll().stream()
                .filter(acc -> acc.getBranch() != null && acc.getBranch().getId().equals(branchId))
                .findFirst();
    }

    // -------------------- XÓA BRANCH KHỎI ACCOUNT --------------------
    @Override
    @Transactional
    public Account removeBranchFromAccount(Long accountId) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            Branch branch = account.getBranch();

            if (branch != null) {
                branch.setVendorAccount(null);
                branchRepository.save(branch);
            }

            account.setBranch(null);
            account.setUpdateDate(LocalDateTime.now());
            return accountRepository.save(account);
        }
        throw new RuntimeException("Không tìm thấy account ID: " + accountId);
    }

    // -------------------- GÁN BRANCH MỚI VỚI THÔNG TIN --------------------
    @Override
    @Transactional
    public Account assignBranchWithInfo(Long accountId, Branch branch) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty()) {
            throw new RuntimeException("Không tìm thấy account ID: " + accountId);
        }

        Account account = optionalAccount.get();
        branch.setActive(true);
        branch.setCreateDate(LocalDateTime.now());
        branch.setUpdateDate(LocalDateTime.now());
        branch.setVendorAccount(account);

        Branch savedBranch = branchRepository.save(branch);
        account.setBranch(savedBranch);
        account.setUpdateDate(LocalDateTime.now());

        return accountRepository.save(account);
    }

    // -------------------- TÌM ACCOUNT THEO ID --------------------
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findAccountById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    // -------------------- LẤY ACCOUNT CHƯA CÓ BRANCH --------------------
    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsWithoutBranch() {
        return accountRepository.findAll().stream()
                .filter(acc -> acc.getBranch() == null)
                .toList();
    }

    // -------------------- THỐNG KÊ DOANH THU CHI NHÁNH --------------------
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBranchRevenueStatistics() {
        List<Object[]> results = entityManager.createQuery("""
            SELECT b.branchName, COALESCE(SUM(bi.totalAmount), 0)
            FROM Bill bi
            JOIN bi.branch b
            WHERE bi.status = com.project.WebAloTra.entity.enumClass.BillStatus.HOAN_THANH
            GROUP BY b.branchName
            ORDER BY SUM(bi.totalAmount) DESC
        """, Object[].class).getResultList();

        List<Map<String, Object>> data = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("branchName", row[0]);
            map.put("totalRevenue", row[1]);
            data.add(map);
        }
        return data;
    }
}
