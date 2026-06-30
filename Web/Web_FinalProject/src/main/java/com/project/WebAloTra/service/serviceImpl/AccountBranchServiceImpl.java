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
import com.project.WebAloTra.repository.CustomerRepository;
import com.project.WebAloTra.repository.RoleRepository;
import com.project.WebAloTra.service.AccountBranchService;
import com.project.WebAloTra.entity.Customer;

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
    private CustomerRepository customerRepository;

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

            // Account là owning side (có branch_id FK), chỉ cần set từ Account
            account.setBranch(branch);
            account.setUpdateDate(LocalDateTime.now());

            return accountRepository.save(account);
        }
        throw new RuntimeException("Không tìm thấy account hoặc branch tương ứng.");
    }

    // -------------------- TẠO MỚI VENDOR ACCOUNT KÈM BRANCH --------------------
    @Override
    @Transactional
    public Account createVendorAccountWithBranch(CreateVendorAccountRequest request) {
        if (accountRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email này đã được sử dụng. Vui lòng dùng tính năng 'Đổi thông tin tài khoản' để gán quyền Vendor cho tài khoản đã có.");
        }
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

        // Account là owning side, chỉ cần save account là đủ
        accountRepository.save(account);

        return account;
    }

    // -------------------- TẠO MỚI VENDOR ACCOUNT ĐỘC LẬP --------------------
    @Override
    @Transactional
    public Account createVendorAccountOnly(com.project.WebAloTra.dto.Account.CreateVendorOnlyRequest request) {
        if (accountRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email này đã được sử dụng. Vui lòng dùng tính năng 'Đổi thông tin tài khoản' để gán quyền Vendor cho tài khoản đã có.");
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setCode(request.getCode());
        account.setNonLocked(true);
        account.setCreateDate(LocalDateTime.now());
        account.setUpdateDate(LocalDateTime.now());

        // Tạo Customer tương ứng để lưu name, phone, code, email
        Customer customer = new Customer();
        customer.setCode(request.getCode());
        customer.setName(request.getName() != null && !request.getName().trim().isEmpty() ? request.getName() : "Vendor " + request.getCode());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer = customerRepository.save(customer);

        account.setCustomer(customer);

        // Nếu có truyền branchId thì lấy branch đó gán vào
        if (request.getBranchId() != null) {
            Optional<Branch> optBranch = branchRepository.findById(request.getBranchId());
            optBranch.ifPresent(account::setBranch);
        }

        Optional<Role> vendorRole = roleRepository.findByName(RoleName.ROLE_VENDOR);
        vendorRole.ifPresent(account::setRole);

        accountRepository.save(account);

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

            // Branch là inverse side, không cần update Branch
            // Chỉ cần set null từ phía Account (owning side)

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
