package com.edm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "更新用户请求")
public record UserUpdateRequest(
        @Schema(description = "用户名") @NotBlank(message = "用户名不能为空") String username,
        @Schema(description = "新密码，留空表示不修改") @Size(min = 8, message = "密码至少 8 位") String password,
        @Schema(description = "姓名") @NotBlank(message = "姓名不能为空") String fullName,
        @Schema(description = "邮箱") @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
        @Schema(description = "SAML Name ID") String samlNameId,
        @Schema(description = "是否启用") boolean enabled,
        @Schema(description = "群组 ID 集合") Set<Long> groupIds
) {
}
