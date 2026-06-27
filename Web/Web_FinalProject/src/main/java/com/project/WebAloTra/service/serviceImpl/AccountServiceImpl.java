package com.project.WebAloTra.service.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.dto.Account.AccountDto;
import com.project.WebAloTra.dto.Account.ChangePasswordDto;
import com.project.WebAloTra.dto.AddressShipping.AddressShippingDto;
import com.project.WebAloTra.dto.Statistic.UserStatistic;
import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.AddressShipping;
import com.project.WebAloTra.entity.Branch;
import com.project.WebAloTra.entity.Customer;
import com.project.WebAloTra.entity.Role;
import com.project.WebAloTra.exception.ShopApiException;
import com.project.WebAloTra.repository.AccountRepository;
import com.project.WebAloTra.repository.AddressShippingRepository;
import com.project.WebAloTra.repository.CustomerRepository;
import com.project.WebAloTra.service.AccountService;
import com.project.WebAloTra.utils.UserLoginUtil;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressShippingRepository addressShippingRepository;

    @Autowired
    private com.project.WebAloTra.repository.RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Account findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public List<Account> findAllAccount() {
        return accountRepository.findAll();
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public List<UserStatistic> getUserStatistics(String startDate, String endDate) {
        List<UserStatistic> userStatistics = new ArrayList<>();

        List<Object[]> results = accountRepository.getMonthlyAccountStatistics(startDate, endDate);

        for (Object[] result : results) {
            String month = (String) result[0];
            Integer count = ((Number) result[1]).intValue();
            userStatistics.add(new UserStatistic(month, count));
        }

        return userStatistics;
    }

    @Override
    public Account blockAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(null);
        account.setNonLocked(false);
        return accountRepository.save(account);
    }

    @Override
    public Account openAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(null);
        account.setNonLocked(true);
        return accountRepository.save(account);
    }

    @Override
    public Account changeRole(String email, Long roleId, Long branchId) {
        Account account = accountRepository.findByEmail(email);
        Account currentLoginUser = UserLoginUtil.getCurrentLogin();

        // Prevent self-demotion and modifying other admins
        if (account.getRole() != null && account.getRole().getId() == 1L) {
            if (currentLoginUser != null && account.getEmail().equalsIgnoreCase(currentLoginUser.getEmail())) {
                if (roleId != 1L) {
                    throw new RuntimeException("Bạn không thể tự giáng cấp quyền của chính mình!");
                }
            } else {
                throw new RuntimeException("Bạn không có quyền thay đổi Role của một Admin khác!");
            }
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Quyền không hợp lệ!"));
        account.setRole(role);

        String roleName = role.getName().name();

        // If role is Staff or Vendor, assign branch if provided
        if ("ROLE_STAFF".equals(roleName) || "ROLE_VENDOR".equals(roleName)) {
            if (branchId != null) {
                if ("ROLE_VENDOR".equals(roleName)) {
                    // Validation: A branch can only have 1 vendor
                    List<Account> branchAccounts = accountRepository.findByBranch_Id(branchId);
                    for (Account acc : branchAccounts) {
                        if (acc.getRole() != null && "ROLE_VENDOR".equals(acc.getRole().getName().name())
                                && !acc.getEmail().equalsIgnoreCase(email)) {
                            throw new RuntimeException("Chi nhánh này đã có Vendor (Người quản lý) khác được gán!");
                        }
                    }
                }

                Branch branch = new Branch();
                branch.setId(branchId);
                account.setBranch(branch);
            }
        } else {
            // For other roles, remove branch assignment
            account.setBranch(null);
        }

        return accountRepository.save(account);
    }

    @Override
    public AccountDto getAccountLogin() {
        Account account = UserLoginUtil.getCurrentLogin();
        Customer customer = customerRepository.findByAccount_Id(account.getId());
        account.setCustomer(customer);
        return convertToDto(account);
    }

    @Override
    public AccountDto updateProfile(AccountDto accountDto) {
        Account account = UserLoginUtil.getCurrentLogin();
        Customer customer = customerRepository.findByAccount_Id(account.getId());
        if (!accountDto.getPhoneNumber().trim().equals(customer.getPhoneNumber())) {
            if (customerRepository.existsByPhoneNumber(accountDto.getPhoneNumber())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST,
                        "Số điện thoại " + accountDto.getPhoneNumber() + " đã được đăng ký");
            }
        }
        customer.setPhoneNumber(accountDto.getPhoneNumber());
        customer.setName(accountDto.getName());
        customerRepository.save(customer);
        return convertToDto(accountRepository.save(account));
    }

    @Override
    public void changePassword(ChangePasswordDto changePasswordDto) {
        Account account = UserLoginUtil.getCurrentLogin();
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), account.getPassword())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác");
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu không khớp");
        }
        account.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public void resetPassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    @Override
    public List<Account> getAccountsWithoutBranch() {
        return accountRepository.findAll().stream()
                .filter(acc -> acc.getBranch() == null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<Account> getVendorAccountsWithoutBranch() {
        return accountRepository.findAll().stream()
                .filter(acc -> acc.getBranch() == null
                        && acc.getRole() != null
                        && "ROLE_VENDOR".equalsIgnoreCase(acc.getRole().getName().name()))
                .collect(java.util.stream.Collectors.toList());
    }

    private AccountDto convertToDto(Account account) {
        AccountDto accountDto = new AccountDto();
        accountDto.setEmail(account.getEmail());
        accountDto.setName(account.getCustomer().getName());
        accountDto.setPhoneNumber(account.getCustomer().getPhoneNumber());
        List<AddressShippingDto> addressShippingDtos = new ArrayList<>();
        List<AddressShipping> addressShippingList = addressShippingRepository
                .findAllByCustomer_Account_Id(account.getId());
        for (AddressShipping addressShipping : addressShippingList) {
            AddressShippingDto addressShippingDto = new AddressShippingDto();
            addressShippingDto.setId(addressShipping.getId());
            addressShippingDto.setAddress(addressShipping.getAddress());
            addressShippingDtos.add(addressShippingDto);
        }
        accountDto.setAddressShippingList(addressShippingDtos);
        return accountDto;
    }

    private Account convertToEntity(AccountDto accountDto) {
        Account account = new Account();
        account.setUpdateDate(LocalDateTime.now());
        account.setEmail(accountDto.getEmail());
        return account;
    }
}