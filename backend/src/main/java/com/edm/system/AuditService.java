package com.edm.system;

import com.edm.security.CurrentUser;
import com.edm.system.entity.AuditLog;
import com.edm.system.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    public AuditService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void record(String action, String targetType, String targetId, String detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperator(CurrentUser.required().username());
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setDetail(detail);
        auditLogMapper.insert(auditLog);
    }
}
