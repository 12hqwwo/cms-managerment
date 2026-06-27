package com.project.WebAloTra.service;


import java.util.List;

import com.project.WebAloTra.dto.Account.AccountDto;
import com.project.WebAloTra.dto.Account.ChangePasswordDto;
import com.project.WebAloTra.dto.Statistic.UserStatistic;
import com.project.WebAloTra.entity.Account;

public interface AccountService {
    Account findByEmail(String email);

    List<Account> findAllAccount();
    Account save(Account account);

    List<UserStatistic> getUserStatistics(String startDate, String endDate);

    Account blockAccount(Long id);

    Account openAccount(Long id);

    Account changeRole(String email, Long roleId, Long branchId);

    AccountDto getAccountLogin();

    AccountDto updateProfile(AccountDto accountDto);

    void changePassword(ChangePasswordDto changePasswordDto);

    void resetPassword(Account account, String newPassword);

    List<Account> getAccountsWithoutBranch();
    
    List<Account> getVendorAccountsWithoutBranch();
}