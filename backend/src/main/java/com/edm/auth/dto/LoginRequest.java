package com.edm.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "用户名密码登录请求")
public record LoginRequest(
        @Schema(description = "用户名") @NotBlank(message = "用户名不能为空") String username,
        @Schema(description = "密码") @NotBlank(message = "密码不能为空") String password
) {
}
