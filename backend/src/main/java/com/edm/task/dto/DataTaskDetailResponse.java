package com.edm.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "数据任务详情")
public record DataTaskDetailResponse(
        Long id,
        String supplierCode,
        String sourceUniqueKey,
        String sourceFileName,
        String sourceRemotePath,
        Long sourceFileSize,
        Long sourceMtime,
        String fetcherType,
        String status,
        String localTempPath,
        int downloadRetryTimes,
        int updateRetryTimes,
        int informRetryTimes,
        String s3Bucket,
        String targetS3Key,
        java.time.LocalDateTime informTime,
        String feedbackFlag,
        String creator,
        java.time.LocalDateTime createTime,
        String modifier,
        java.time.LocalDateTime updateTime
) {
}
