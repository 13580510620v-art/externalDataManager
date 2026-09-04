package com.edm.system;

import com.edm.TestRedissonConfiguration;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.LoginUser;
import com.edm.system.dto.UserCreateRequest;
import com.edm.system.dto.UserResponse;
import com.edm.system.dto.UserUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        clean();
        login("user:read", "user:write", "group:read");
        jdbcTemplate.update("""
                INSERT INTO SYS_GROUP (ID, GROUP_CODE, GROUP_NAME, DESCRIPTION, IS_ENABLE)
                VALUES (9101, 'USER_TEST_GROUP', '用户测试群组', '用户服务测试', 1)
                """);
    }

    @AfterEach
    void tearDown() {
        clean();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUserHashesPasswordAssignsGroupsAndWritesAudit() {
        UserResponse response = userService.create(new UserCreateRequest(
                "new-user",
                "New@123456",
                "新用户",
                "new-user@example.com",
                "saml-new-user",
                true,
                Set.of(9101L)
        ));

        String passwordHash = jdbcTemplate.queryForObject(
                "SELECT PASSWORD_HASH FROM SYS_USER WHERE ID = ?",
                String.class,
                response.id()
        );
        assertThat(passwordHash).isNotEqualTo("New@123456");
        assertThat(passwordEncoder.matches("New@123456", passwordHash)).isTrue();
        assertThat(response.username()).isEqualTo("new-user");
        assertThat(response.groupIds()).containsExactly(9101L);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM AUDIT_LOG WHERE ACTION = 'user.create' AND TARGET_ID = ?",
                Integer.class,
                response.id()
        )).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateUsername() {
        userService.create(new UserCreateRequest(
                "duplicate-user",
                "New@123456",
                "重复用户",
                "duplicate-user@example.com",
                null,
                true,
                Set.of()
        ));

        assertThatThrownBy(() -> userService.create(new UserCreateRequest(
                "duplicate-user",
                "New@123456",
                "重复用户",
                "another@example.com",
                null,
                true,
                Set.of()
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USERNAME_EXISTS);
    }

    @Test
    void rejectsDisablingLastEnabledAdmin() {
        Long adminGroupId = jdbcTemplate.queryForObject(
                "SELECT ID FROM SYS_GROUP WHERE GROUP_CODE = 'ADMIN'",
                Long.class
        );
        jdbcTemplate.update("""
                INSERT INTO SYS_USER (ID, USERNAME, PASSWORD_HASH, FULL_NAME, EMAIL, IS_ENABLE)
                VALUES (9201, 'last-admin', 'hash', '最后管理员', 'last-admin@example.com', 1)
                """);
        jdbcTemplate.update("INSERT INTO SYS_USER_GROUP (USER_ID, GROUP_ID) VALUES (9201, ?)", adminGroupId);

        assertThatThrownBy(() -> userService.setEnabled(9201L, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("最后一个启用的系统管理员");
    }

    @Test
    void rejectsRemovingLastEnabledAdminFromAdminGroup() {
        Long adminGroupId = jdbcTemplate.queryForObject(
                "SELECT ID FROM SYS_GROUP WHERE GROUP_CODE = 'ADMIN'",
                Long.class
        );
        jdbcTemplate.update("""
                INSERT INTO SYS_USER (ID, USERNAME, PASSWORD_HASH, FULL_NAME, EMAIL, IS_ENABLE)
                VALUES (9202, 'last-admin-group', 'hash', '最后管理员', 'last-admin-group@example.com', 1)
                """);
        jdbcTemplate.update("INSERT INTO SYS_USER_GROUP (USER_ID, GROUP_ID) VALUES (9202, ?)", adminGroupId);

        assertThatThrownBy(() -> userService.update(9202L, new UserUpdateRequest(
                "last-admin-group",
                "New@123456",
                "最后管理员",
                "last-admin-group@example.com",
                null,
                true,
                Set.of(9101L)
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("最后一个启用的系统管理员");
    }

    @Test
    void rejectsWriteWithoutPermission() {
        login("user:read");

        assertThatThrownBy(() -> userService.create(new UserCreateRequest(
                "no-permission-user",
                "New@123456",
                "无权用户",
                "no-permission@example.com",
                null,
                true,
                Set.of()
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private void login(String... permissions) {
        LoginUser user = new LoginUser(
                9999L,
                "tester",
                "测试用户",
                true,
                Set.of(permissions)
        );
        List<SimpleGrantedAuthority> authorities = user.permissions().stream()
                .map(permission -> new SimpleGrantedAuthority("PERM_" + permission))
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, authorities)
        );
    }

    private void clean() {
        jdbcTemplate.update("DELETE FROM AUDIT_LOG WHERE TARGET_ID IN ('9201', '9202')");
        jdbcTemplate.update("DELETE FROM SYS_USER_GROUP WHERE USER_ID IN (SELECT ID FROM SYS_USER WHERE USERNAME IN ('new-user', 'duplicate-user', 'no-permission-user', 'last-admin', 'last-admin-group'))");
        jdbcTemplate.update("DELETE FROM SYS_USER WHERE USERNAME IN ('new-user', 'duplicate-user', 'no-permission-user', 'last-admin', 'last-admin-group')");
        jdbcTemplate.update("DELETE FROM SYS_USER_GROUP WHERE GROUP_ID = 9101");
        jdbcTemplate.update("DELETE FROM SYS_GROUP WHERE ID = 9101");
    }
}
