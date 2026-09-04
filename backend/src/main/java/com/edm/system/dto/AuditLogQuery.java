package com.edm.system.dto;

public record AuditLogQuery(
        String keyword,
        long page,
        long size
) {
}
