package com.edm.dashboard;

import com.edm.TestRedissonConfiguration;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.LoginUser;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clean();
        login("dashboard:read");
        insertTask(9701L, "SUP-A", "COMPLETED", LocalDate.now().atTime(8, 0));
        insertTask(9702L, "SUP-A", "PENDING", LocalDate.now().atTime(9, 0));
        insertTask(9703L, "SUP-B", "DOWNLOAD_FAILED", LocalDate.now().atTime(10, 0));
        insertTask(9704L, "SUP-B", "COMPLETED", LocalDate.now().atTime(11, 0));
        insertTask(9705L, "SUP-OLD", "COMPLETED", LocalDate.now().minusDays(1).atTime(12, 0));
    }

    @AfterEach
    void tearDown() {
        clean();
        SecurityContextHolder.clearContext();
    }

    @Test
    void summarizesTodayTasksOnly() {
        DashboardSummaryResponse summary = dashboardService.today();

        assertThat(summary.date()).isEqualTo(LocalDate.now());
        assertThat(summary.total()).isEqualTo(4);
        assertThat(summary.completed()).isEqualTo(2);
        assertThat(summary.successRate()).isEqualTo(0.5);
        assertThat(summary.statusCounts())
                .containsEntry("COMPLETED", 2L)
                .containsEntry("PENDING", 1L)
                .containsEntry("DOWNLOAD_FAILED", 1L)
                .doesNotContainKey("OLD");
        assertThat(summary.supplierCounts())
                .containsEntry("SUP-A", 2L)
                .containsEntry("SUP-B", 2L)
                .doesNotContainKey("SUP-OLD");
        assertThat(summary.recentTasks()).hasSize(5);
        assertThat(summary.recentTasks().get(0).id()).isEqualTo(9704L);
        assertThat(summary.recentTasks().get(4).id()).isEqualTo(9705L);
    }

    @Test
    void rejectsReadWithoutPermission() {
        login("task:read");

        assertThatThrownBy(() -> dashboardService.today())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private void insertTask(Long id, String supplierCode, String status, LocalDateTime createTime) {
        jdbcTemplate.update("""
                INSERT INTO DATA_TASK (
                    ID, SUPPLIER_CODE, SOURCE_UNIQUE_KEY, SOURCE_FILE_NAME, FETCHER_TYPE, STATUS,
                    DOWNLOAD_RETRY_TIMES, UPDATE_RETRY_TIMES, INFORM_RETRY_TIMES, FEEDBACK_FLAG,
                    CREATE_TIME, UPDATE_TIME
                ) VALUES (?, ?, ?, ?, 'SFTP', ?, 0, 0, 0, 'N', ?, ?)
                """,
                id,
                supplierCode,
                "unique-" + id,
                "file-" + id + ".csv",
                status,
                createTime,
                createTime
        );
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
        jdbcTemplate.update("DELETE FROM DATA_TASK WHERE ID IN (9701, 9702, 9703, 9704, 9705)");
    }
}
