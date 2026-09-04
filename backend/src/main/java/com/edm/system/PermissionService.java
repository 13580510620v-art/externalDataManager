package com.edm.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.PermissionEvaluator;
import com.edm.system.dto.PermissionResponse;
import com.edm.system.entity.Permission;
import com.edm.system.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionMapper permissionMapper;
    private final PermissionEvaluator permissionEvaluator;

    public PermissionService(PermissionMapper permissionMapper, PermissionEvaluator permissionEvaluator) {
        this.permissionMapper = permissionMapper;
        this.permissionEvaluator = permissionEvaluator;
    }

    public List<PermissionResponse> list() {
        permissionEvaluator.require("permission:read");
        return permissionMapper.selectList(new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getPermissionCode(),
                permission.getPermissionName(),
                permission.getResourceType(),
                permission.getAction(),
                Boolean.TRUE.equals(permission.getEnabled())
        );
    }
}
