package com.edm.security;

import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PermissionEvaluator {

    private static final String PERMISSION_PREFIX = "PERM_";

    public boolean has(String permissionCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String expectedAuthority = PERMISSION_PREFIX + permissionCode;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> expectedAuthority.equals(authority) || permissionCode.equals(authority));
    }

    public void require(String permissionCode) {
        if (!has(permissionCode)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
