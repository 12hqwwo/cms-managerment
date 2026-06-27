package com.project.WebAloTra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.repository.AccountRepository;
import com.project.WebAloTra.repository.RoleRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository; 
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== DANG NHAP VOI EMAIL: [" + username + "]");
        Account account = accountRepository.findByEmail(username);

        if (account != null) {
            System.out.println("=== TIM THAY ACCOUNT TRONG DB: " + account.getEmail() + " | Hash: " + account.getPassword() + " | Active: " + account.isNonLocked());
            return new CustomUserDetails(account);
        }

        System.out.println("=== KHONG TIM THAY TAI KHOAN TRONG DB VOI EMAIL: [" + username + "]");
        throw new UsernameNotFoundException("Không tìm thấy tài khoản có email: " + username);
    }
}
