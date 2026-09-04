package com.edm.task;

import com.edm.TestRedissonConfiguration;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.LoginUser;
import com.edm.system.AuditService;
import com.edm.task.dto.DataTaskQuery;
import com.edm.task.dto.DataTaskResponse;
import com.edm.task.dto.DataTaskDetailResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class DataTaskServiceTest {

    @Autowired
    private DataTaskService dataTaskService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clean();
        login("task:read", "task:retry");
        insertTask(9501L, "SUP-A", "DOWNLOAD_FAILED", "download.csv", 2, 0, 0);
        insertTask(9502L, "SUP-B", "UPLOAD_FAILED", "upload.csv", 0, 3, 0);
        insertTask(9503L, "SUP-C", "INFORM_FAILED", "inform.csv", 0, 0, 4);
        insertTask(9504L, "SUP-A", "PENDING", "pending.csv", 0, 0, 0);
    }

    @AfterEach
    void tearDown() {
        clean();
        SecurityContextHolder.clearContext();
    }

    @Test
    void retriesEachFailureTypeAndResetsStatusToPending() {
        dataTaskService.retry(9501L);
        dataTaskService.retry(9502L);
        dataTaskService.retry(9503L);

        assertThat(taskField(9501L, "DOWNLOAD_RETRY_TIMES")).isEqualTo(3);
        assertThat(taskField(9502L, "UPDATE_RETRY_TIMES")).isEqualTo(4);
        assertThat(taskField(9503L, "INFORM_RETRY_TIMES")).isEqualTo(5);
        assertThat(taskField(9501L, "STATUS")).isEqualTo("PENDING");
        assertThat(taskField(9502L, "STATUS")).isEqualTo("PENDING");
        assertThat(taskField(9503L, "STATUS")).isEqualTo("PENDING");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM AUDIT_LOG WHERE ACTION = 'task.retry' AND TARGET_ID IN ('9501', '9502', '9503')",
                Integer.class
        )).isEqualTo(3);
    }

    @Test
    void rejectsRetryForNonFailedTask() {
        assertThatThrownBy(() -> dataTaskService.retry(9504L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TASK_RETRY_NOT_ALLOWED);
    }

    @Test
    void pagesTasksWithFilters() {
        List<DataTaskResponse> records = dataTaskService
                .page(new DataTaskQuery("SUP-A", null, null, null, null, null, 1, 10))
                .getRecords();

        assertThat(records)
                .extracting(DataTaskResponse::supplierCode)
                .containsOnly("SUP-A");
        assertThat(records).hasSize(2);
    }

    @Test
    void returnsDetail() {
        DataTaskDetailResponse detail = dataTaskService.detail(9501L);

        assertThat(detail.sourceFileName()).isEqualTo("download.csv");
        assertThat(detail.status()).isEqualTo("DOWNLOAD_FAILED");
    }

    @Test
    void rejectsRetryWithoutPermission() {
        login("task:read");

        assertThatThrownBy(() -> dataTaskService.retry(9501L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private void insertTask(
            Long id,
            String supplierCode,
            String status,
            String fileName,
            int downloadRetry,
            int updateRetry,
            int informRetry
    ) {
        jdbcTemplate.update("""
                INSERT INTO DATA_TASK (
                    ID, SUPPLIER_CODE, SOURCE_UNIQUE_KEY, SOURCE_FILE_NAME, SOURCE_REMOTE_PATH,
                    SOURCE_FILE_SIZE, SOURCE_MTIME, FETCHER_TYPE, STATUS, DOWNLOAD_RETRY_TIMES,
                    UPDATE_RETRY_TIMES, INFORM_RETRY_TIMES, S3_BUCKET, TARGET_S3_KEY, FEEDBACK_FLAG,
                    CREATE_TIME, UPDATE_TIME
                ) VALUES (?, ?, ?, ?, NULL, 1, 0, 'SFTP', ?, ?, ?, ?, NULL, NULL, 'N', ?, ?)
                """,
                id,
                supplierCode,
                "unique-" + id,
                fileName,
                status,
                downloadRetry,
                updateRetry,
                informRetry,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Object taskField(Long id, String field) {
        return jdbcTemplate.queryForObject(
                "SELECT " + field + " FROM DATA_TASK WHERE ID = ?",
                Object.class,
                id
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
        jdbcTemplate.update("DELETE FROM AUDIT_LOG WHERE TARGET_TYPE = 'DATA_TASK'");
        jdbcTemplate.update("DELETE FROM DATA_TASK WHERE ID IN (9501, 9502, 9503, 9504)");
    }
}
