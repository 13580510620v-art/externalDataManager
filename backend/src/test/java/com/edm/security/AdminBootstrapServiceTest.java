package com.edm.security;

import com.edm.TestRedissonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class AdminBootstrapServiceTest {

    @Autowired
    private AdminBootstrapService adminBootstrapService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        resetPlaceholder();
    }

    @AfterEach
    void tearDown() {
        resetPlaceholder();
    }

    @Test
    void enablesBootstrapAdminWithHashedPassword() {
        adminBootstrapService.bootstrap("bootstrap-admin", "Bootstrap@123");

        assertThat(userField("USERNAME", "bootstrap-admin")).isEqualTo("bootstrap-admin");
        assertThat(userField("IS_ENABLE", "bootstrap-admin")).isEqualTo(1);
        String passwordHash = String.valueOf(userField("PASSWORD_HASH", "bootstrap-admin"));
        assertThat(passwordHash).isNotEqualTo("Bootstrap@123");
        assertThat(passwordEncoder.matches("Bootstrap@123", passwordHash)).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM SYS_USER_GROUP ug
                JOIN SYS_GROUP g ON g.ID = ug.GROUP_ID
                JOIN SYS_USER u ON u.ID = ug.USER_ID
                WHERE u.USERNAME = 'bootstrap-admin' AND g.GROUP_CODE = 'ADMIN'
                """,
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void keepsPlaceholderDisabledWhenEnvironmentIsMissing() {
        adminBootstrapService.bootstrap(null, null);

        assertThat(userField("USERNAME", "__ADMIN_BOOTSTRAP__")).isEqualTo("__ADMIN_BOOTSTRAP__");
        assertThat(userField("IS_ENABLE", "__ADMIN_BOOTSTRAP__")).isEqualTo(0);
        assertThat(userField("PASSWORD_HASH", "__ADMIN_BOOTSTRAP__")).isNull();
    }

    private void resetPlaceholder() {
        jdbcTemplate.update("DELETE FROM SYS_USER_GROUP WHERE USER_ID IN (SELECT ID FROM SYS_USER WHERE EMAIL = 'admin@external-data.local')");
        jdbcTemplate.update("DELETE FROM SYS_USER WHERE EMAIL = 'admin@external-data.local'");
        jdbcTemplate.update("""
                INSERT INTO SYS_USER (USERNAME, PASSWORD_HASH, FULL_NAME, EMAIL, SAML_NAME_ID, IS_ENABLE)
                VALUES ('__ADMIN_BOOTSTRAP__', NULL, '系统管理员', 'admin@external-data.local', NULL, 0)
                """);
        jdbcTemplate.update("""
                INSERT INTO SYS_USER_GROUP (USER_ID, GROUP_ID)
                SELECT u.ID, g.ID
                FROM SYS_USER u
                JOIN SYS_GROUP g ON g.GROUP_CODE = 'ADMIN'
                WHERE u.EMAIL = 'admin@external-data.local'
                """);
    }

    private Object userField(String field, String username) {
        return jdbcTemplate.queryForObject(
                "SELECT " + field + " FROM SYS_USER WHERE USERNAME = ?",
                Object.class,
                username
        );
    }
}
