package com.edm.auth;

import com.edm.auth.dto.CurrentUserResponse;
import com.edm.auth.dto.LoginRequest;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.LoginCandidate;
import com.edm.security.LoginUser;
import com.edm.security.UserAuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String passwordHash = passwordEncoder.encode("Admin@123");
    private final LoginUser enabledUser = new LoginUser(
            1L,
            "admin",
            "系统管理员",
            true,
            Set.of("dashboard:read", "user:write")
    );
    private final UserAuthenticationService userAuthenticationService = username ->
            "admin".equals(username)
                    ? Optional.of(new LoginCandidate(enabledUser, passwordHash))
                    : Optional.empty();
    private final AuthService authService = new AuthService(
            userAuthenticationService,
            passwordEncoder
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginReturnsEnabledUserWhenPasswordMatches() {
        LoginUser user = authService.login(new LoginRequest("admin", "Admin@123"));

        assertThat(user).isEqualTo(enabledUser);
    }

    @Test
    void loginFailsForUnknownUserOrWrongPassword() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("missing", "Admin@123")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);
        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    void currentUserComesFromSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                enabledUser,
                "token",
                List.of(new SimpleGrantedAuthority("PERM_dashboard:read"))
        ));

        CurrentUserResponse response = authService.currentUser();

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.fullName()).isEqualTo("系统管理员");
        assertThat(response.permissions()).containsExactlyInAnyOrder("dashboard:read", "user:write");
    }
}
