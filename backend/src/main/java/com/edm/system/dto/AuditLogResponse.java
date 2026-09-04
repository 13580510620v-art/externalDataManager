package com.edm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "审计日志")
public record AuditLogResponse(
        @Schema(description = "日志 ID") Long id,
        @Schema(description = "操作人") String operator,
        @Schema(description = "操作动作") String action,
        @Schema(description = "目标类型") String targetType,
        @Schema(description = "目标 ID") String targetId,
        @Schema(description = "详情") String detail,
        @Schema(description = "创建时间") LocalDateTime createTime
) {
}
