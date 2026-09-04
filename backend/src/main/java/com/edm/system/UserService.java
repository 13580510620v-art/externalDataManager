package com.edm.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.CurrentUser;
import com.edm.security.PermissionEvaluator;
import com.edm.system.dto.UserCreateRequest;
import com.edm.system.dto.UserQuery;
import com.edm.system.dto.UserResponse;
import com.edm.system.dto.UserUpdateRequest;
import com.edm.system.entity.Group;
import com.edm.system.entity.User;
import com.edm.system.mapper.GroupMapper;
import com.edm.system.mapper.UserMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static final String ADMIN_GROUP_CODE = "ADMIN";

    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final PermissionEvaluator permissionEvaluator;
    private final AuditService auditService;

    public UserService(
            UserMapper userMapper,
            GroupMapper groupMapper,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            PermissionEvaluator permissionEvaluator,
            AuditService auditService
    ) {
        this.userMapper = userMapper;
        this.groupMapper = groupMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.permissionEvaluator = permissionEvaluator;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public IPage<UserResponse> page(UserQuery query) {
        permissionEvaluator.require("user:read");
        long page = Math.max(query.page(), 1);
        long size = query.size() <= 0 ? 10 : Math.min(query.size(), 100);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = query.keyword().trim();
            wrapper.and(condition -> condition
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getFullName, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        if (query.enabled() != null) {
            wrapper.eq(User::getEnabled, query.enabled());
        }
        if (query.groupId() != null) {
            List<Long> userIds = jdbcTemplate.queryForList(
                    "SELECT USER_ID FROM SYS_USER_GROUP WHERE GROUP_ID = ?",
                    Long.class,
                    query.groupId()
            );
            if (userIds.isEmpty()) {
                return new Page<UserResponse>(page, size);
            }
            wrapper.in(User::getId, userIds);
        }
        wrapper.orderByDesc(User::getId);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserResponse> responses = result.getRecords().stream().map(this::toResponse).toList();
        Page<UserResponse> responsePage = new Page<>(page, size, result.getTotal());
        responsePage.setRecords(responses);
        return responsePage;
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        permissionEvaluator.require("user:write");
        assertUnique(request.username(), request.email(), request.samlNameId(), null);
        User user = new User();
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim());
        user.setSamlNameId(normalize(request.samlNameId()));
        user.setEnabled(request.enabled());
        userMapper.insert(user);
        Set<Long> groupIds = normalizeIds(request.groupIds());
        validateGroups(groupIds);
        replaceUserGroups(user.getId(), groupIds);
        auditService.record("user.create", "USER", String.valueOf(user.getId()), request.username());
        return toResponse(userMapper.selectById(user.getId()));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        permissionEvaluator.require("user:write");
        User existing = requireUser(id);
        assertUnique(request.username(), request.email(), request.samlNameId(), id);
        Set<Long> oldGroupIds = loadGroupIds(id);
        Set<Long> newGroupIds = normalizeIds(request.groupIds());
        assertNotRemovingLastAdmin(id, oldGroupIds, newGroupIds);
        validateGroups(newGroupIds);
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id)
                .set(User::getUsername, request.username().trim())
                .set(User::getFullName, request.fullName().trim())
                .set(User::getEmail, request.email().trim())
                .set(User::getSamlNameId, normalize(request.samlNameId()))
                .set(User::getEnabled, request.enabled());
        if (request.password() != null && !request.password().isBlank()) {
            wrapper.set(User::getPasswordHash, passwordEncoder.encode(request.password()));
        }
        userMapper.update(null, wrapper);
        replaceUserGroups(id, newGroupIds);
        auditService.record("user.update", "USER", String.valueOf(id), existing.getUsername());
        return toResponse(userMapper.selectById(id));
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        permissionEvaluator.require("user:write");
        User existing = requireUser(id);
        if (!enabled && Boolean.TRUE.equals(existing.getEnabled())) {
            assertNotDisablingLastAdmin(id);
        }
        userMapper.update(
                null,
                new LambdaUpdateWrapper<User>().eq(User::getId, id).set(User::getEnabled, enabled)
        );
        auditService.record(
                enabled ? "user.enable" : "user.disable",
                "USER",
                String.valueOf(id),
                existing.getUsername()
        );
        return toResponse(userMapper.selectById(id));
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void assertUnique(String username, String email, String samlNameId, Long excludedId) {
        if (exists(User::getUsername, username.trim(), excludedId)) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (exists(User::getEmail, email.trim(), excludedId)) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        String nameId = normalize(samlNameId);
        if (nameId != null && exists(User::getSamlNameId, nameId, excludedId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "SAML Name ID 已存在");
        }
    }

    private boolean exists(
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<User, ?> column,
            String value,
            Long excludedId
    ) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(column, value);
        if (excludedId != null) {
            wrapper.ne(User::getId, excludedId);
        }
        return userMapper.selectCount(wrapper) > 0;
    }

    private void assertNotDisablingLastAdmin(Long userId) {
        Group adminGroup = findAdminGroup();
        if (adminGroup == null || !loadGroupIds(userId).contains(adminGroup.getId())) {
            return;
        }
        if (!hasOtherEnabledAdmin(adminGroup.getId(), userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能禁用最后一个启用的系统管理员");
        }
    }

    private void assertNotRemovingLastAdmin(Long userId, Set<Long> oldGroupIds, Set<Long> newGroupIds) {
        Group adminGroup = findAdminGroup();
        if (adminGroup == null || !oldGroupIds.contains(adminGroup.getId()) || newGroupIds.contains(adminGroup.getId())) {
            return;
        }
        if (!hasOtherEnabledAdmin(adminGroup.getId(), userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能移除最后一个启用的系统管理员的系统管理员群组");
        }
    }

    private Group findAdminGroup() {
        return groupMapper.selectOne(
                new LambdaQueryWrapper<Group>().eq(Group::getGroupCode, ADMIN_GROUP_CODE)
        );
    }

    private boolean hasOtherEnabledAdmin(Long adminGroupId, Long userId) {
        String sql = """
                SELECT u.ID
                FROM SYS_USER u
                JOIN SYS_USER_GROUP ug ON ug.USER_ID = u.ID
                WHERE ug.GROUP_ID = ?
                  AND u.ID <> ?
                  AND u.IS_ENABLE = 1
                FOR UPDATE
                """;
        return !jdbcTemplate.queryForList(sql, Long.class, adminGroupId, userId).isEmpty();
    }

    private void validateGroups(Set<Long> groupIds) {
        if (groupIds.isEmpty()) {
            return;
        }
        if (groupMapper.selectBatchIds(groupIds).size() != groupIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群组不存在");
        }
    }

    private void replaceUserGroups(Long userId, Set<Long> groupIds) {
        jdbcTemplate.update("DELETE FROM SYS_USER_GROUP WHERE USER_ID = ?", userId);
        for (Long groupId : groupIds) {
            jdbcTemplate.update("INSERT INTO SYS_USER_GROUP (USER_ID, GROUP_ID) VALUES (?, ?)", userId, groupId);
        }
    }

    private Set<Long> loadGroupIds(Long userId) {
        return new HashSet<>(jdbcTemplate.queryForList(
                "SELECT GROUP_ID FROM SYS_USER_GROUP WHERE USER_ID = ? ORDER BY GROUP_ID",
                Long.class,
                userId
        ));
    }

    private Set<Long> normalizeIds(Set<Long> ids) {
        return ids == null ? Set.of() : new HashSet<>(ids);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getSamlNameId(),
                Boolean.TRUE.equals(user.getEnabled()),
                loadGroupIds(user.getId()),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }
}
