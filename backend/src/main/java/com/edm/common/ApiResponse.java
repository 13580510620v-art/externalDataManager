package com.edm.common;

import com.edm.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "统一 API 响应")
public record ApiResponse<T>(
        @Schema(description = "业务状态码，0 表示成功") int code,
        @Schema(description = "提示信息") String message,
        @Schema(description = "响应数据") T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(0, "success", null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), message, null);
    }
}
