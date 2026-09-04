package com.edm.security;

import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionEvaluatorTest {

    private final PermissionEvaluator permissionEvaluator = new PermissionEvaluator();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasPermissionChecksAuthorityWithPrefix() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "token",
                        List.of(new SimpleGrantedAuthority("PERM_supplier:read")))
        );

        assertThat(permissionEvaluator.has("supplier:read")).isTrue();
        assertThat(permissionEvaluator.has("supplier:write")).isFalse();
    }

    @Test
    void requirePermissionRejectsMissingPermission() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "token",
                        List.of(new SimpleGrantedAuthority("PERM_supplier:read")))
        );

        assertThatThrownBy(() -> permissionEvaluator.require("supplier:write"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }
}
