package com.edm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminBootstrapService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);
    private static final String BOOTSTRAP_EMAIL = "admin@external-data.local";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminBootstrapService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${edm.admin.username:}") String adminUsername,
            @Value("${edm.admin.password:}") String adminPassword
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        bootstrap(adminUsername, adminPassword);
    }

    @Transactional
    public void bootstrap(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("未配置 EDM_ADMIN_USERNAME 或 EDM_ADMIN_PASSWORD，管理员密码登录保持禁用");
            return;
        }
        if (password.length() < 8) {
            throw new IllegalStateException("管理员初始密码至少 8 位");
        }
        Long bootstrapUserId = jdbcTemplate.queryForObject(
                "SELECT ID FROM SYS_USER WHERE EMAIL = ?",
                Long.class,
                BOOTSTRAP_EMAIL
        );
        if (bootstrapUserId == null) {
            throw new IllegalStateException("管理员引导记录不存在，请检查 Flyway 迁移");
        }
        Integer conflictCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SYS_USER WHERE USERNAME = ? AND ID <> ?",
                Integer.class,
                username,
                bootstrapUserId
        );
        if (conflictCount != null && conflictCount > 0) {
            throw new IllegalStateException("管理员用户名已存在，请更换 EDM_ADMIN_USERNAME");
        }
        jdbcTemplate.update("""
                        UPDATE SYS_USER
                        SET USERNAME = ?, PASSWORD_HASH = ?, FULL_NAME = '系统管理员', IS_ENABLE = 1, UPDATE_TIME = CURRENT_TIMESTAMP
                        WHERE ID = ?
                        """,
                username,
                passwordEncoder.encode(password),
                bootstrapUserId
        );
        jdbcTemplate.update("""
                        INSERT INTO SYS_USER_GROUP (USER_ID, GROUP_ID)
                        SELECT ?, g.ID
                        FROM SYS_GROUP g
                        WHERE g.GROUP_CODE = 'ADMIN'
                          AND NOT EXISTS (
                              SELECT 1 FROM SYS_USER_GROUP ug
                              WHERE ug.USER_ID = ? AND ug.GROUP_ID = g.ID
                          )
                        """,
                bootstrapUserId,
                bootstrapUserId
        );
        log.info("管理员引导完成：{}", username);
    }
}
