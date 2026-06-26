package com.project.WebAloTra.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.project.WebAloTra.security.CustomUserDetails;

@Service
public class BranchIsolationService {

    /**
     * Get the current user's branch ID from SecurityContext
     */
    public Long getCurrentBranchId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
            return user.getBranchId();
        }
        return null;
    }

    /**
     * Check if the current user has ROLE_STAFF
     */
    public boolean isStaff() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
            return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
        }
        return false;
    }

    /**
     * Check if the current user has ROLE_ADMIN
     */
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
            return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }

    /**
     * Enforce branch isolation logic (Pseudo-VPD).
     * If user is ADMIN, they can access anything.
     * If user is STAFF/VENDOR, they can only access data belonging to their assigned branch.
     * Throws an exception if access is denied.
     */
    public void enforceBranchIsolation(Long targetBranchId) {
        if (isAdmin()) {
            return; // Admin can access any branch
        }
        
        Long currentBranchId = getCurrentBranchId();
        if (currentBranchId == null || !currentBranchId.equals(targetBranchId)) {
            throw new RuntimeException("Access Denied: You can only access data for your assigned branch (Pseudo-VPD active).");
        }
    }
}
