package com.edm.exception;

import com.edm.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionKeepsErrorCodeAndMessage() {
        ApiResponse<Void> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.SUPPLIER_CODE_EXISTS, "供应商编码已存在"));

        assertThat(response.code()).isEqualTo(ErrorCode.SUPPLIER_CODE_EXISTS.getCode());
        assertThat(response.message()).isEqualTo("供应商编码已存在");
    }

    @Test
    void accessDeniedReturnsForbiddenResponse() {
        ApiResponse<Void> response = handler.handleAccessDenied(new AccessDeniedException(" denied"));

        assertThat(response.code()).isEqualTo(ErrorCode.FORBIDDEN.getCode());
        assertThat(response.message()).isEqualTo("没有权限执行该操作");
    }
}
