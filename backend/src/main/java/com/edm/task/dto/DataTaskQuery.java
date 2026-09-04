package com.edm.task.dto;

import java.time.LocalDateTime;

public record DataTaskQuery(
        String supplierCode,
        String status,
        String fileName,
        String feedbackFlag,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long page,
        long size
) {
}
