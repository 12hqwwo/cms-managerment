package com.project.WebAloTra.security;

import com.project.WebAloTra.entity.Account;
import com.project.WebAloTra.repository.AccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * VpdContextFilter — Chạy mỗi request, set Oracle Application Context cho VPD và OLS.
 *
 * VPD (Virtual Private Database) — file 3, 4, 5:
 *   - Gọi pkg_vpd_security.set_account_id() để set ctx_trasua.account_id + customer_id
 *   - Nếu thiếu → VPD policy trả '1=2' → cart/address/bill của user đều rỗng
 *
 * OLS (Oracle Label Security) — file 8:
 *   - Gọi SA_SESSION.SET_LABEL() theo role
 *   - STAFF xem bill mức STAFF+, MANAGER xem MANAGER+, DIRECTOR xem tất cả
 */
@Component
public class VpdContextFilter extends OncePerRequestFilter {

    private final AccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VpdContextFilter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            try {
                Account account = accountRepository.findByEmail(email);
                if (account != null) {
                    // ===== FIX 3 (VPD): Set account_id + customer_id vào context =====
                    // Sau khi set, fn_vpd_cart / fn_vpd_address / fn_vpd_bill hoạt động đúng
                    entityManager.createNativeQuery(
                        "BEGIN TRASUA.pkg_vpd_security.set_account_id(:accountId); END;"
                    ).setParameter("accountId", account.getId())
                     .executeUpdate();

                    // ===== FIX 5 (OLS): Set label theo role cho bảng BILL =====
                    // STAFF  → chỉ thấy bill mức STAFF (amount < 500k)
                    // VENDOR → thấy bill mức MANAGER (amount >= 500k)
                    // ADMIN  → thấy tất cả bill (DIRECTOR)
                    String olsLabel = resolveOlsLabel(account);
                    try {
                        entityManager.createNativeQuery(
                            "BEGIN SA_SESSION.SET_LABEL('BILL_OLS_POL', :label); END;"
                        ).setParameter("label", olsLabel)
                         .executeUpdate();
                    } catch (Exception olsEx) {
                        // OLS chưa được cài đặt hoặc chưa apply policy → bỏ qua, không làm crash app
                        logger.warn("OLS SET_LABEL bị lỗi (có thể OLS chưa được apply): " + olsEx.getMessage());
                    }
                }
            } catch (Exception ex) {
                // VPD context lỗi → log warning, không làm crash request
                logger.warn("VPD context set thất bại: " + ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Map role của account sang OLS label string.
     * Khớp với 3 levels đã tạo trong 8_OLS_BILL.sql:
     *   STAFF(10), MANAGER(20), DIRECTOR(30)
     */
    private String resolveOlsLabel(Account account) {
        if (account.getRole() == null || account.getRole().getName() == null) {
            return "STAFF";
        }
        String roleName = account.getRole().getName().name(); // enum → String
        return switch (roleName) {
            case "ROLE_ADMIN"  -> "DIRECTOR";
            case "ROLE_VENDOR" -> "MANAGER";
            default            -> "STAFF";   // ROLE_STAFF, ROLE_USER, ROLE_GUEST
        };
    }
}
