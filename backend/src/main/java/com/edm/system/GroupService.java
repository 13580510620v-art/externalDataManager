package com.edm.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.PermissionEvaluator;
import com.edm.system.dto.GroupCreateRequest;
import com.edm.system.dto.GroupResponse;
import com.edm.system.dto.GroupUpdateRequest;
import com.edm.system.entity.Group;
import com.edm.system.entity.Permission;
import com.edm.system.mapper.GroupMapper;
import com.edm.system.mapper.PermissionMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupService {

    private static final String ADMIN_GROUP_CODE = "ADMIN";
    private static final Set<String> ADMIN_REQUIRED_PERMISSIONS = Set.of(
            "user:write",
            "group:write",
            "permission:read"
    );

    private final GroupMapper groupMapper;
    private final PermissionMapper permissionMapper;
    private final JdbcTemplate jdbcTemplate;
    private final PermissionEvaluator permissionEvaluator;
    private final AuditService auditService;

    public GroupService(
            GroupMapper groupMapper,
            PermissionMapper permissionMapper,
            JdbcTemplate jdbcTemplate,
            PermissionEvaluator permissionEvaluator,
            AuditService auditService
    ) {
        this.groupMapper = groupMapper;
        this.permissionMapper = permissionMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.permissionEvaluator = permissionEvaluator;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public IPage<GroupResponse> page(long page, long size, String keyword, Boolean enabled) {
        permissionEvaluator.require("group:read");
        long currentPage = Math.max(page, 1);
        long pageSize = size <= 0 ? 10 : Math.min(size, 100);
        LambdaQueryWrapper<Group> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            wrapper.and(condition -> condition
                    .like(Group::getGroupCode, value)
                    .or()
                    .like(Group::getGroupName, value));
        }
        if (enabled != null) {
            wrapper.eq(Group::getEnabled, enabled);
        }
        wrapper.orderByDesc(Group::getId);
        Page<Group> result = groupMapper.selectPage(new Page<>(currentPage, pageSize), wrapper);
        Page<GroupResponse> responsePage = new Page<>(currentPage, pageSize, result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).toList());
        return responsePage;
    }

    @Transactional
    public GroupResponse create(GroupCreateRequest request) {
        permissionEvaluator.require("group:write");
        assertUniqueGroupCode(request.groupCode().trim(), null);
        Group group = new Group();
        group.setGroupCode(request.groupCode().trim());
        group.setGroupName(request.groupName().trim());
        group.setDescription(request.description());
        group.setEnabled(request.enabled());
        groupMapper.insert(group);
        Set<Long> permissionIds = normalizeIds(request.permissionIds());
        validatePermissions(permissionIds);
        replaceGroupPermissions(group.getId(), permissionIds);
        auditService.record("group.create", "GROUP", String.valueOf(group.getId()), request.groupCode());
        return toResponse(groupMapper.selectById(group.getId()));
    }

    @Transactional
    public GroupResponse update(Long id, GroupUpdateRequest request) {
        permissionEvaluator.require("group:write");
        Group existing = requireGroup(id);
        assertUniqueGroupCode(request.groupCode().trim(), id);
        if (ADMIN_GROUP_CODE.equals(existing.getGroupCode())
                && (!ADMIN_GROUP_CODE.equals(request.groupCode().trim()) || !request.enabled())) {
            throw new BusinessException(ErrorCode.CONFLICT, "系统管理员群组不能禁用或变更编码");
        }
        groupMapper.update(
                null,
                new LambdaUpdateWrapper<Group>()
                        .eq(Group::getId, id)
                        .set(Group::getGroupCode, request.groupCode().trim())
                        .set(Group::getGroupName, request.groupName().trim())
                        .set(Group::getDescription, request.description())
                        .set(Group::getEnabled, request.enabled())
        );
        auditService.record("group.update", "GROUP", String.valueOf(id), existing.getGroupCode());
        return toResponse(groupMapper.selectById(id));
    }

    @Transactional
    public GroupResponse assignPermissions(Long id, Set<Long> permissionIds) {
        permissionEvaluator.require("group:write");
        Group group = requireGroup(id);
        Set<Long> ids = normalizeIds(permissionIds);
        validatePermissions(ids);
        if (ADMIN_GROUP_CODE.equals(group.getGroupCode())) {
            assertAdminPermissionsKept(ids);
        }
        replaceGroupPermissions(id, ids);
        auditService.record("group.assign-permissions", "GROUP", String.valueOf(id), group.getGroupCode());
        return toResponse(groupMapper.selectById(id));
    }

    private Group requireGroup(Long id) {
        Group group = groupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群组不存在");
        }
        return group;
    }

    private void assertUniqueGroupCode(String groupCode, Long excludedId) {
        LambdaQueryWrapper<Group> wrapper = new LambdaQueryWrapper<Group>().eq(Group::getGroupCode, groupCode);
        if (excludedId != null) {
            wrapper.ne(Group::getId, excludedId);
        }
        if (groupMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.GROUP_CODE_EXISTS);
        }
    }

    private void validatePermissions(Set<Long> permissionIds) {
        if (permissionIds.isEmpty()) {
            return;
        }
        if (permissionMapper.selectBatchIds(permissionIds).size() != permissionIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "权限不存在");
        }
    }

    private void replaceGroupPermissions(Long groupId, Set<Long> permissionIds) {
        jdbcTemplate.update("DELETE FROM SYS_GROUP_PERMISSION WHERE GROUP_ID = ?", groupId);
        for (Long permissionId : permissionIds) {
            jdbcTemplate.update(
                    "INSERT INTO SYS_GROUP_PERMISSION (GROUP_ID, PERMISSION_ID) VALUES (?, ?)",
                    groupId,
                    permissionId
            );
        }
    }

    private Set<Long> requiredPermissionIds() {
        return new HashSet<>(permissionMapper.selectList(
                        new LambdaQueryWrapper<Permission>().in(Permission::getPermissionCode, ADMIN_REQUIRED_PERMISSIONS))
                .stream()
                .map(Permission::getId)
                .toList());
    }

    private void assertAdminPermissionsKept(Set<Long> nextPermissionIds) {
        if (!nextPermissionIds.containsAll(requiredPermissionIds())) {
            throw new BusinessException(ErrorCode.CONFLICT, "系统管理员群组必须保留用户、群组和权限管理能力");
        }
    }

    private Set<Long> loadPermissionIds(Long groupId) {
        return new HashSet<>(jdbcTemplate.queryForList(
                "SELECT PERMISSION_ID FROM SYS_GROUP_PERMISSION WHERE GROUP_ID = ? ORDER BY PERMISSION_ID",
                Long.class,
                groupId
        ));
    }

    private Set<String> loadPermissionCodes(Long groupId) {
        return new HashSet<>(jdbcTemplate.queryForList(
                """
                        SELECT p.PERMISSION_CODE
                        FROM SYS_GROUP_PERMISSION gp
                        JOIN SYS_PERMISSION p ON p.ID = gp.PERMISSION_ID
                        WHERE gp.GROUP_ID = ?
                        """,
                String.class,
                groupId
        ));
    }

    private Set<Long> normalizeIds(Set<Long> ids) {
        return ids == null ? Set.of() : new HashSet<>(ids);
    }

    private GroupResponse toResponse(Group group) {
        Set<Long> permissionIds = loadPermissionIds(group.getId());
        Set<String> permissionCodes = loadPermissionCodes(group.getId());
        return new GroupResponse(
                group.getId(),
                group.getGroupCode(),
                group.getGroupName(),
                group.getDescription(),
                Boolean.TRUE.equals(group.getEnabled()),
                permissionIds,
                permissionCodes,
                group.getCreateTime(),
                group.getUpdateTime()
        );
    }
}
