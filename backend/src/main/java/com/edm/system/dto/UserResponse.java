package com.edm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "用户信息")
public record UserResponse(
        @Schema(description = "用户 ID") Long id,
        @Schema(description = "用户名") String username,
        @Schema(description = "姓名") String fullName,
        @Schema(description = "邮箱") String email,
        @Schema(description = "SAML Name ID") String samlNameId,
        @Schema(description = "是否启用") boolean enabled,
        @Schema(description = "群组 ID 集合") Set<Long> groupIds,
        @Schema(description = "创建时间") LocalDateTime createTime,
        @Schema(description = "更新时间") LocalDateTime updateTime
) {
}
