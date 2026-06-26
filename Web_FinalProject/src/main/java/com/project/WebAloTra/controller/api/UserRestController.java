package com.project.WebAloTra.controller.api;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.repository.AccountRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserRestController {

	private final AccountRepository accountRepository;

    public UserRestController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/api/current-user")
    public Map<String, Object> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> result = new HashMap<>();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Account acc = accountRepository.findByEmail(auth.getName());
            result.put("username", acc != null ? acc.getEmail() : auth.getName());
        } else {
            result.put("username", "Guest");
        }

        return result;
    }
}
