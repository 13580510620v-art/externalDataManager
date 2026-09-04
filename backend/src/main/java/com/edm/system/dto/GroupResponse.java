package com.edm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "群组信息")
public record GroupResponse(
        @Schema(description = "群组 ID") Long id,
        @Schema(description = "群组编码") String groupCode,
        @Schema(description = "群组名称") String groupName,
        @Schema(description = "描述") String description,
        @Schema(description = "是否启用") boolean enabled,
        @Schema(description = "权限 ID 集合") Set<Long> permissionIds,
        @Schema(description = "权限编码集合") Set<String> permissionCodes,
        @Schema(description = "创建时间") LocalDateTime createTime,
        @Schema(description = "更新时间") LocalDateTime updateTime
) {
}
