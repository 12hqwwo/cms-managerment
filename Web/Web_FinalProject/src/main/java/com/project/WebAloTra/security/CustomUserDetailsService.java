package com.project.WebAloTra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.repository.AccountRepository;
import com.project.WebAloTra.repository.RoleRepository;

import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository; 
    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Reset VPD & OLS trên đúng Connection này trước khi query để tránh bị dính rác từ Connection Pool
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (java.sql.CallableStatement cs3 = connection.prepareCall("{call TRASUA.pkg_branch_sec.set_context(-1, '', -1)}")) {
                cs3.execute();
            } catch (Exception ex) {}
            try (java.sql.CallableStatement cs4 = connection.prepareCall("{call SA_SESSION.SET_LABEL('ACCESS_POLICY', 'SEC:BR1,BR2:MNG,OPR')}")) {
                cs4.execute();
            } catch (Exception ex) {}
        });

        Account account = accountRepository.findByEmail(username);

        if (account != null) {
           
            return new CustomUserDetails(account);
        }

        throw new UsernameNotFoundException("Không tìm thấy tài khoản có email: " + username);
    }
}
