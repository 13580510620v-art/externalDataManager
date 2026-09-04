package com.edm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Schema(description = "创建群组请求")
public record GroupCreateRequest(
        @Schema(description = "群组编码") @NotBlank(message = "群组编码不能为空") String groupCode,
        @Schema(description = "群组名称") @NotBlank(message = "群组名称不能为空") String groupName,
        @Schema(description = "描述") String description,
        @Schema(description = "是否启用") boolean enabled,
        @Schema(description = "权限 ID 集合") Set<Long> permissionIds
) {
}
