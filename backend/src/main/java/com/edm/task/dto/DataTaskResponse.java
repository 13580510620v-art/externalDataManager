package com.edm.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "数据任务信息")
public record DataTaskResponse(
        @Schema(description = "任务 ID") Long id,
        @Schema(description = "供应商编码") String supplierCode,
        @Schema(description = "源文件唯一键") String sourceUniqueKey,
        @Schema(description = "源文件名") String sourceFileName,
        @Schema(description = "远端路径") String sourceRemotePath,
        @Schema(description = "源文件大小") Long sourceFileSize,
        @Schema(description = "源文件修改时间戳") Long sourceMtime,
        @Schema(description = "文件获取类型") String fetcherType,
        @Schema(description = "状态") String status,
        @Schema(description = "下载重试次数") int downloadRetryTimes,
        @Schema(description = "上传重试次数") int updateRetryTimes,
        @Schema(description = "通知重试次数") int informRetryTimes,
        @Schema(description = "S3 Bucket") String s3Bucket,
        @Schema(description = "S3 Key") String targetS3Key,
        @Schema(description = "通知时间") LocalDateTime informTime,
        @Schema(description = "下游反馈标识") String feedbackFlag,
        @Schema(description = "创建时间") LocalDateTime createTime,
        @Schema(description = "更新时间") LocalDateTime updateTime
) {
}
