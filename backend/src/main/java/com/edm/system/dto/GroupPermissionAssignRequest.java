package com.edm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "分配群组权限请求")
public record GroupPermissionAssignRequest(
        @Schema(description = "权限 ID 集合") Set<Long> permissionIds
) {
}
