package com.edm.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "当前用户信息")
public record CurrentUserResponse(
        @Schema(description = "用户 ID") Long id,
        @Schema(description = "用户名") String username,
        @Schema(description = "姓名") String fullName,
        @Schema(description = "权限编码集合") Set<String> permissions
) {
}
