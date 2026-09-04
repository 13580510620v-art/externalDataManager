package com.edm.supplier;

import com.edm.common.ApiResponse;
import com.edm.common.PageResponse;
import com.edm.supplier.dto.SupplierCreateRequest;
import com.edm.supplier.dto.SupplierQuery;
import com.edm.supplier.dto.SupplierResponse;
import com.edm.supplier.dto.SupplierUpdateRequest;
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

@Tag(name = "供应商管理", description = "外部数据供应商配置")
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Operation(summary = "分页查询供应商")
    @GetMapping
    public ApiResponse<PageResponse<SupplierResponse>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            String keyword,
            String fetcherType,
            Boolean enabled
    ) {
        return ApiResponse.success(PageResponse.of(supplierService.page(
                new SupplierQuery(keyword, fetcherType, enabled, page, size)
        )));
    }

    @Operation(summary = "创建供应商")
    @PostMapping
    public ApiResponse<SupplierResponse> create(@Valid @RequestBody SupplierCreateRequest request) {
        return ApiResponse.success(supplierService.create(request));
    }

    @Operation(summary = "更新供应商")
    @PostMapping("/{id}/update")
    public ApiResponse<SupplierResponse> update(
            @Parameter(description = "供应商 ID") @PathVariable Long id,
            @Valid @RequestBody SupplierUpdateRequest request
    ) {
        return ApiResponse.success(supplierService.update(id, request));
    }

    @Operation(summary = "启用供应商")
    @PostMapping("/{id}/enable")
    public ApiResponse<SupplierResponse> enable(
            @Parameter(description = "供应商 ID") @PathVariable Long id
    ) {
        return ApiResponse.success(supplierService.setEnabled(id, true));
    }

    @Operation(summary = "禁用供应商")
    @PostMapping("/{id}/disable")
    public ApiResponse<SupplierResponse> disable(
            @Parameter(description = "供应商 ID") @PathVariable Long id
    ) {
        return ApiResponse.success(supplierService.setEnabled(id, false));
    }
}
