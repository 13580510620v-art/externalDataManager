package com.edm.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.edm.TestRedissonConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class DatabaseUserAuthenticationServiceTest {

    @Autowired
    private DatabaseUserAuthenticationService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clean();
        jdbcTemplate.update("""
                INSERT INTO SYS_USER (ID, USERNAME, PASSWORD_HASH, FULL_NAME, EMAIL, SAML_NAME_ID, IS_ENABLE)
                VALUES (9001, 'auth-user', 'password-hash', '认证用户', 'auth-user@example.com', 'saml-auth-user', 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO SYS_GROUP (ID, GROUP_CODE, GROUP_NAME, DESCRIPTION, IS_ENABLE)
                VALUES (9001, 'AUTH_TEST_GROUP', '认证测试群组', '认证测试', 1)
                """);
        jdbcTemplate.update("""
                INSERT INTO SYS_PERMISSION (ID, PERMISSION_CODE, PERMISSION_NAME, RESOURCE_TYPE, ACTION, IS_ENABLE)
                VALUES (9001, 'auth:read', '认证读取', 'auth', 'read', 1)
                """);
        jdbcTemplate.update("INSERT INTO SYS_USER_GROUP (USER_ID, GROUP_ID) VALUES (9001, 9001)");
        jdbcTemplate.update("INSERT INTO SYS_GROUP_PERMISSION (GROUP_ID, PERMISSION_ID) VALUES (9001, 9001)");
    }

    @AfterEach
    void tearDown() {
        clean();
    }

    @Test
    void findsPasswordCandidateAndPermissionsByUsername() {
        LoginCandidate candidate = service.findByUsername("auth-user").orElseThrow();

        assertThat(candidate.passwordHash()).isEqualTo("password-hash");
        assertThat(candidate.user().id()).isEqualTo(9001L);
        assertThat(candidate.user().username()).isEqualTo("auth-user");
        assertThat(candidate.user().enabled()).isTrue();
        assertThat(candidate.user().permissions()).containsExactly("auth:read");
    }

    @Test
    void findsSamlUserByNameId() {
        LoginUser user = service.findByNameId("saml-auth-user").orElseThrow();

        assertThat(user.username()).isEqualTo("auth-user");
        assertThat(user.permissions()).containsExactly("auth:read");
    }

    private void clean() {
        jdbcTemplate.update("DELETE FROM SYS_GROUP_PERMISSION WHERE GROUP_ID = 9001 OR PERMISSION_ID = 9001");
        jdbcTemplate.update("DELETE FROM SYS_USER_GROUP WHERE USER_ID = 9001 OR GROUP_ID = 9001");
        jdbcTemplate.update("DELETE FROM SYS_PERMISSION WHERE ID = 9001");
        jdbcTemplate.update("DELETE FROM SYS_GROUP WHERE ID = 9001");
        jdbcTemplate.update("DELETE FROM SYS_USER WHERE ID = 9001");
    }
}
