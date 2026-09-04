package com.edm.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edm.security.PermissionEvaluator;
import com.edm.system.dto.AuditLogQuery;
import com.edm.system.dto.AuditLogResponse;
import com.edm.system.entity.AuditLog;
import com.edm.system.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final PermissionEvaluator permissionEvaluator;

    public AuditLogService(AuditLogMapper auditLogMapper, PermissionEvaluator permissionEvaluator) {
        this.auditLogMapper = auditLogMapper;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> page(AuditLogQuery query) {
        permissionEvaluator.require("audit:read");
        long currentPage = Math.max(query.page(), 1);
        long pageSize = query.size() <= 0 ? 10 : Math.min(query.size(), 100);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        if (query.keyword() != null && !query.keyword().isBlank()) {
            wrapper.and(condition -> condition
                    .like(AuditLog::getAction, query.keyword().trim())
                    .or()
                    .like(AuditLog::getOperator, query.keyword().trim())
                    .or()
                    .like(AuditLog::getTargetType, query.keyword().trim())
                    .or()
                    .like(AuditLog::getTargetId, query.keyword().trim()));
        }
        wrapper.orderByDesc(AuditLog::getCreateTime).orderByDesc(AuditLog::getId);
        Page<AuditLog> result = auditLogMapper.selectPage(new Page<>(currentPage, pageSize), wrapper);
        Page<AuditLogResponse> responsePage = new Page<>(currentPage, pageSize, result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).toList());
        return responsePage;
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getOperator(),
                auditLog.getAction(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getDetail(),
                auditLog.getCreateTime()
        );
    }
}
