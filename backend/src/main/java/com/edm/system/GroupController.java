package com.edm.system;

import com.edm.common.ApiResponse;
import com.edm.common.PageResponse;
import com.edm.system.dto.GroupCreateRequest;
import com.edm.system.dto.GroupResponse;
import com.edm.system.dto.GroupUpdateRequest;
import com.edm.system.dto.GroupPermissionAssignRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "群组管理", description = "系统群组和权限集合管理")
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(summary = "分页查询群组")
    @GetMapping
    public ApiResponse<PageResponse<GroupResponse>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            String keyword,
            Boolean enabled
    ) {
        return ApiResponse.success(PageResponse.of(groupService.page(page, size, keyword, enabled)));
    }

    @Operation(summary = "创建群组")
    @PostMapping
    public ApiResponse<GroupResponse> create(@Valid @RequestBody GroupCreateRequest request) {
        return ApiResponse.success(groupService.create(request));
    }

    @Operation(summary = "更新群组")
    @PostMapping("/{id}/update")
    public ApiResponse<GroupResponse> update(
            @Parameter(description = "群组 ID") @PathVariable Long id,
            @Valid @RequestBody GroupUpdateRequest request
    ) {
        return ApiResponse.success(groupService.update(id, request));
    }

    @Operation(summary = "分配群组权限")
    @PostMapping("/{id}/permissions")
    public ApiResponse<GroupResponse> assignPermissions(
            @Parameter(description = "群组 ID") @PathVariable Long id,
            @Valid @RequestBody GroupPermissionAssignRequest request
    ) {
        return ApiResponse.success(groupService.assignPermissions(id, request.permissionIds()));
    }
}
