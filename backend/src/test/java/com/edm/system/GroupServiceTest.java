package com.edm.system;

import com.edm.TestRedissonConfiguration;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.LoginUser;
import com.edm.system.dto.GroupCreateRequest;
import com.edm.system.dto.GroupUpdateRequest;
import com.edm.system.dto.GroupResponse;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class GroupServiceTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clean();
        login("group:read", "group:write", "permission:read");
        jdbcTemplate.update("""
                INSERT INTO SYS_PERMISSION (ID, PERMISSION_CODE, PERMISSION_NAME, RESOURCE_TYPE, ACTION, IS_ENABLE)
                VALUES (9301, 'group:test', '群组测试权限', 'group', 'test', 1)
                """);
    }

    @AfterEach
    void tearDown() {
        clean();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsGroupAndAssignsPermissionsWithAudit() {
        GroupResponse response = groupService.create(new GroupCreateRequest(
                "TEST_GROUP",
                "测试群组",
                "群组服务测试",
                true,
                Set.of(9301L)
        ));

        assertThat(response.groupCode()).isEqualTo("TEST_GROUP");
        assertThat(response.permissionIds()).containsExactly(9301L);
        assertThat(response.permissionCodes()).containsExactly("group:test");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM AUDIT_LOG WHERE ACTION = 'group.create' AND TARGET_ID = ?",
                Integer.class,
                response.id()
        )).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateGroupCode() {
        groupService.create(new GroupCreateRequest(
                "DUPLICATE_GROUP",
                "重复群组",
                null,
                true,
                Set.of()
        ));

        assertThatThrownBy(() -> groupService.create(new GroupCreateRequest(
                "DUPLICATE_GROUP",
                "重复群组",
                null,
                true,
                Set.of()
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GROUP_CODE_EXISTS);
    }

    @Test
    void rejectsWriteWithoutPermission() {
        login("group:read");

        assertThatThrownBy(() -> groupService.create(new GroupCreateRequest(
                "NO_PERMISSION_GROUP",
                "无权群组",
                null,
                true,
                Set.of()
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void rejectsDisablingAdminGroup() {
        Long adminGroupId = jdbcTemplate.queryForObject(
                "SELECT ID FROM SYS_GROUP WHERE GROUP_CODE = 'ADMIN'",
                Long.class
        );

        assertThatThrownBy(() -> groupService.update(adminGroupId, new GroupUpdateRequest(
                "ADMIN",
                "系统管理员",
                null,
                false
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("系统管理员群组不能禁用");
    }

    @Test
    void rejectsRemovingAdminManagementPermissions() {
        Long adminGroupId = jdbcTemplate.queryForObject(
                "SELECT ID FROM SYS_GROUP WHERE GROUP_CODE = 'ADMIN'",
                Long.class
        );
        Long userWritePermissionId = jdbcTemplate.queryForObject(
                "SELECT ID FROM SYS_PERMISSION WHERE PERMISSION_CODE = 'user:write'",
                Long.class
        );

        assertThatThrownBy(() -> groupService.assignPermissions(adminGroupId, Set.of(userWritePermissionId)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("系统管理员群组必须保留");
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

    private void clean() {
        jdbcTemplate.update("DELETE FROM AUDIT_LOG WHERE TARGET_ID IN ('9401', '9402')");
        jdbcTemplate.update("DELETE FROM SYS_GROUP_PERMISSION WHERE GROUP_ID IN (SELECT ID FROM SYS_GROUP WHERE GROUP_CODE IN ('TEST_GROUP', 'DUPLICATE_GROUP', 'NO_PERMISSION_GROUP'))");
        jdbcTemplate.update("DELETE FROM SYS_USER_GROUP WHERE GROUP_ID IN (SELECT ID FROM SYS_GROUP WHERE GROUP_CODE IN ('TEST_GROUP', 'DUPLICATE_GROUP', 'NO_PERMISSION_GROUP'))");
        jdbcTemplate.update("DELETE FROM SYS_GROUP WHERE GROUP_CODE IN ('TEST_GROUP', 'DUPLICATE_GROUP', 'NO_PERMISSION_GROUP')");
        jdbcTemplate.update("DELETE FROM SYS_PERMISSION WHERE ID = 9301");
    }
}
