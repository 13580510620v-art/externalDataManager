package com.edm.system;

import com.edm.TestRedissonConfiguration;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.LoginUser;
import com.edm.system.dto.PermissionResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class PermissionServiceTest {

    @Autowired
    private PermissionService permissionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listsPermissionsForAuthorizedUser() {
        login("permission:read");

        List<PermissionResponse> permissions = permissionService.list();

        assertThat(permissions)
                .extracting(PermissionResponse::permissionCode)
                .contains("dashboard:read", "supplier:read", "task:read");
    }

    @Test
    void rejectsListWithoutPermission() {
        login("dashboard:read");

        assertThatThrownBy(() -> permissionService.list())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private void login(String... permissions) {
        LoginUser user = new LoginUser(9999L, "tester", "测试用户", true, Set.of(permissions));
        List<SimpleGrantedAuthority> authorities = user.permissions().stream()
                .map(permission -> new SimpleGrantedAuthority("PERM_" + permission))
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, authorities)
        );
    }
}
