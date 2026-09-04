package com.edm.system;

import com.edm.common.ApiResponse;
import com.edm.common.PageResponse;
import com.edm.system.dto.UserCreateRequest;
import com.edm.system.dto.UserQuery;
import com.edm.system.dto.UserResponse;
import com.edm.system.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理", description = "系统用户账号管理")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "分页查询用户")
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            String keyword,
            Long groupId,
            Boolean enabled
    ) {
        return ApiResponse.success(PageResponse.of(userService.page(
                new UserQuery(keyword, groupId, enabled, page, size)
        )));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @Operation(summary = "更新用户")
    @PostMapping("/{id}/update")
    public ApiResponse<UserResponse> update(
            @Parameter(description = "用户 ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ApiResponse.success(userService.update(id, request));
    }

    @Operation(summary = "启用用户")
    @PostMapping("/{id}/enable")
    public ApiResponse<UserResponse> enable(@Parameter(description = "用户 ID") @PathVariable Long id) {
        return ApiResponse.success(userService.setEnabled(id, true));
    }

    @Operation(summary = "禁用用户")
    @PostMapping("/{id}/disable")
    public ApiResponse<UserResponse> disable(@Parameter(description = "用户 ID") @PathVariable Long id) {
        return ApiResponse.success(userService.setEnabled(id, false));
    }
}
