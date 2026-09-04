package com.edm.supplier;

import com.edm.TestRedissonConfiguration;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.LoginUser;
import com.edm.supplier.dto.SupplierCreateRequest;
import com.edm.supplier.dto.SupplierResponse;
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
class SupplierServiceTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clean();
        login("supplier:read", "supplier:write");
    }

    @AfterEach
    void tearDown() {
        clean();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsSupplierWithAudit() {
        SupplierResponse response = supplierService.create(new SupplierCreateRequest(
                "SUP-A",
                "供应商 A",
                "SFTP",
                "/incoming",
                10,
                "^SUP-A-.*\\.csv$",
                "edm-supplier-a"
        ));

        assertThat(response.supplierCode()).isEqualTo("SUP-A");
        assertThat(response.fetcherType()).isEqualTo("SFTP");
        assertThat(response.enabled()).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM AUDIT_LOG WHERE ACTION = 'supplier.create' AND TARGET_ID = ?",
                Integer.class,
                response.id()
        )).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateSupplierCode() {
        supplierService.create(new SupplierCreateRequest(
                "SUP-DUP",
                "重复供应商",
                "SFTP",
                null,
                10,
                null,
                null
        ));

        assertThatThrownBy(() -> supplierService.create(new SupplierCreateRequest(
                "SUP-DUP",
                "另一个供应商",
                "REST",
                null,
                10,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SUPPLIER_CODE_EXISTS);
    }

    @Test
    void rejectsInvalidFetcherTypeAndFrequency() {
        assertThatThrownBy(() -> supplierService.create(new SupplierCreateRequest(
                "SUP-BAD-TYPE",
                "类型错误",
                "FTP",
                null,
                10,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("文件获取类型只允许 SFTP 或 REST");

        assertThatThrownBy(() -> supplierService.create(new SupplierCreateRequest(
                "SUP-BAD-FREQUENCY",
                "频率错误",
                "SFTP",
                null,
                0,
                null,
                null
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("上传频率必须大于等于 1 分钟");
    }

    @Test
    void rejectsWriteWithoutPermission() {
        login("supplier:read");

        assertThatThrownBy(() -> supplierService.create(new SupplierCreateRequest(
                "SUP-NO-PERMISSION",
                "无权供应商",
                "SFTP",
                null,
                10,
                null,
                null
        )))
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

    private void clean() {
        jdbcTemplate.update("DELETE FROM AUDIT_LOG WHERE TARGET_TYPE = 'SUPPLIER'");
        jdbcTemplate.update("DELETE FROM EXTERNAL_SUPPLIER WHERE SUPPLIER_CODE IN ('SUP-A', 'SUP-DUP', 'SUP-BAD-TYPE', 'SUP-BAD-FREQUENCY', 'SUP-NO-PERMISSION')");
    }
}
