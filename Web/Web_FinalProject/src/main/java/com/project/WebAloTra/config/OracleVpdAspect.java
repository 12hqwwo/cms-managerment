package com.project.WebAloTra.config;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.CallableStatement;
import java.sql.Connection;

@Aspect
@Component
public class OracleVpdAspect {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Chạy trước mọi method trong Repository hoặc các method @Transactional
     * để set context cho Oracle VPD và OLS
     */
    @Before("execution(* com.project.WebAloTra.repository.*.*(..))")
    public void setOracleContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            // Lấy username đang đăng nhập
            String username = auth.getName();
            
            Session session = entityManager.unwrap(Session.class);
            session.doWork((Connection connection) -> {
                try {
                    // 1. OLS Context (Set Client Identifier cho policy VPD_USERS)
                    try (CallableStatement call = connection.prepareCall("{call DBMS_SESSION.SET_IDENTIFIER(?)}")) {
                        call.setString(1, username);
                        call.execute();
                    }

                    // 2. VPD Context (Set Account ID & Customer ID)
                    Long accountId = null;
                    Long customerId = null;
                    String roleName = null;
                    try (java.sql.PreparedStatement ps = connection.prepareStatement(
                            "SELECT a.id, a.customer_id, r.name FROM account a JOIN role r ON a.role_id = r.id WHERE a.email = ?")) {
                        ps.setString(1, username);
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                accountId = rs.getLong("id");
                                
                                customerId = rs.getLong("customer_id");
                                if (rs.wasNull()) { customerId = null; }
                                
                                roleName = rs.getString("name");
                            }
                        }
                    }

                    // Set Account ID
                    if (accountId != null) {
                        try (CallableStatement call = connection.prepareCall("{call TRASUA.TRASUA_CONTEXT_PKG.SET_ACCOUNT_ID(?)}")) {
                            if (roleName != null && (roleName.equals("ROLE_ADMIN") || roleName.equals("ROLE_VENDOR"))) {
                                call.setLong(1, 0); // 0 = Admin/Vendor bypass
                            } else {
                                call.setLong(1, accountId);
                            }
                            call.execute();
                        }
                    }
                    
                    // Set Customer ID
                    if (customerId != null) {
                        try (CallableStatement call = connection.prepareCall("{call TRASUA.TRASUA_CONTEXT_PKG.SET_CUSTOMER_ID(?)}")) {
                            if (roleName != null && (roleName.equals("ROLE_ADMIN") || roleName.equals("ROLE_VENDOR"))) {
                                call.setLong(1, 0); // 0 = Admin/Vendor bypass
                            } else {
                                call.setLong(1, customerId);
                            }
                            call.execute();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi set Oracle Context: " + e.getMessage());
                }
            });
        }
    }
}
