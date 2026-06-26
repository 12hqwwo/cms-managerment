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

        Account account = accountRepository.findByEmail(username);

        if (account != null) {
           
            return new CustomUserDetails(account);
        }

        throw new UsernameNotFoundException("Không tìm thấy tài khoản có email: " + username);
    }
}
