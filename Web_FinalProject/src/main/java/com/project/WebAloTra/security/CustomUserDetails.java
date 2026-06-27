package com.project.WebAloTra.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.entity.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class CustomUserDetails implements UserDetails {
	private Account account;

	public CustomUserDetails(Account account) {
		this.account = account;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
	    Role role = account.getRole();
	    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

	    if (role != null) {
	        String roleName = role.getName().name();

	        // 🔧 Nếu đã có "ROLE_" thì giữ nguyên, không thêm nữa
	        if (!roleName.startsWith("ROLE_")) {
	            roleName = "ROLE_" + roleName;
	        }

	        authorities.add(new SimpleGrantedAuthority(roleName));
	    } else {
	    }

	    return authorities;
	}



	@Override
	public String getPassword() {
		return account.getPassword();
	}

	@Override
	public String getUsername() {
		return account.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return account.isNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	public Long getBranchId() {
	    if (account != null && account.getBranch() != null) {
	        return account.getBranch().getId();
	    }
	    return null;
	}



}
