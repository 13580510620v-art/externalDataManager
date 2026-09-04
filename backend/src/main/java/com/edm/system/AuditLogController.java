package com.edm.system;

import com.edm.common.ApiResponse;
import com.edm.common.PageResponse;
import com.edm.system.dto.AuditLogQuery;
import com.edm.system.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "日志管理", description = "管理端操作审计日志")
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "分页查询审计日志")
    @GetMapping
    public ApiResponse<PageResponse<AuditLogResponse>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            String keyword
    ) {
        return ApiResponse.success(PageResponse.of(auditLogService.page(new AuditLogQuery(keyword, page, size))));
    }
}
