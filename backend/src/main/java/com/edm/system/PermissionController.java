package com.edm.system;

import com.edm.common.ApiResponse;
import com.edm.system.dto.PermissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "权限管理", description = "系统权限查询")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(summary = "查询权限列表")
    @GetMapping
    public ApiResponse<List<PermissionResponse>> list() {
        return ApiResponse.success(permissionService.list());
    }
}
