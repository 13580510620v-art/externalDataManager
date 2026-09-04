package com.edm.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "当日任务汇总")
public record DashboardSummaryResponse(
        @Schema(description = "统计日期") LocalDate date,
        @Schema(description = "任务总数") long total,
        @Schema(description = "完成数量") long completed,
        @Schema(description = "成功率，0 到 1") double successRate,
        @Schema(description = "状态分布") Map<String, Long> statusCounts,
        @Schema(description = "供应商分布") Map<String, Long> supplierCounts,
        @Schema(description = "较上周同日总数变化") long totalChange,
        @Schema(description = "较上周同日失败数变化") long failureChange,
        @Schema(description = "最近上传记录") List<DashboardRecentTaskResponse> recentTasks
) {
}
