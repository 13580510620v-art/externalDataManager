package com.edm.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "最近上传记录")
public record DashboardRecentTaskResponse(
        @Schema(description = "任务 ID") Long id,
        @Schema(description = "供应商名称") String supplierName,
        @Schema(description = "源业务文件名") String sourceFileName,
        @Schema(description = "源文件字节大小") Long sourceFileSize,
        @Schema(description = "任务状态") String status,
        @Schema(description = "创建时间") LocalDateTime createTime
) {
}
