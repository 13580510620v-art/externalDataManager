package com.edm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "权限信息")
public record PermissionResponse(
        @Schema(description = "权限 ID") Long id,
        @Schema(description = "权限编码") String permissionCode,
        @Schema(description = "权限名称") String permissionName,
        @Schema(description = "资源类型") String resourceType,
        @Schema(description = "操作") String action,
        @Schema(description = "是否启用") boolean enabled
) {
}
