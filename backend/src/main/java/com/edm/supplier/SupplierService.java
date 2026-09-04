package com.edm.supplier;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.CurrentUser;
import com.edm.security.PermissionEvaluator;
import com.edm.supplier.dto.SupplierCreateRequest;
import com.edm.supplier.dto.SupplierQuery;
import com.edm.supplier.dto.SupplierResponse;
import com.edm.supplier.dto.SupplierUpdateRequest;
import com.edm.system.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class SupplierService {

    private static final Set<String> FETCHER_TYPES = Set.of("SFTP", "REST");

    private final SupplierMapper supplierMapper;
    private final PermissionEvaluator permissionEvaluator;
    private final AuditService auditService;

    public SupplierService(
            SupplierMapper supplierMapper,
            PermissionEvaluator permissionEvaluator,
            AuditService auditService
    ) {
        this.supplierMapper = supplierMapper;
        this.permissionEvaluator = permissionEvaluator;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public IPage<SupplierResponse> page(SupplierQuery query) {
        permissionEvaluator.require("supplier:read");
        long currentPage = Math.max(query.page(), 1);
        long pageSize = query.size() <= 0 ? 10 : Math.min(query.size(), 100);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (query.keyword() != null && !query.keyword().isBlank()) {
            String keyword = query.keyword().trim();
            wrapper.and(condition -> condition
                    .like(Supplier::getSupplierCode, keyword)
                    .or()
                    .like(Supplier::getSupplierName, keyword));
        }
        if (query.fetcherType() != null && !query.fetcherType().isBlank()) {
            wrapper.eq(Supplier::getFetcherType, query.fetcherType().trim());
        }
        if (query.enabled() != null) {
            wrapper.eq(Supplier::getEnabled, query.enabled());
        }
        wrapper.orderByDesc(Supplier::getId);
        Page<Supplier> result = supplierMapper.selectPage(new Page<>(currentPage, pageSize), wrapper);
        Page<SupplierResponse> responsePage = new Page<>(currentPage, pageSize, result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).toList());
        return responsePage;
    }

    @Transactional
    public SupplierResponse create(SupplierCreateRequest request) {
        permissionEvaluator.require("supplier:write");
        String supplierCode = request.supplierCode().trim();
        assertUniqueCode(supplierCode, null);
        validate(request.fetcherType(), request.updateFrequency());
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(supplierCode);
        supplier.setSupplierName(request.supplierName().trim());
        supplier.setFetcherType(request.fetcherType().trim());
        supplier.setRemoteSubDir(normalizeRemoteSubDir(request.fetcherType(), request.remoteSubDir()));
        supplier.setEnabled(true);
        supplier.setUpdateFrequency(request.updateFrequency());
        supplier.setFileNameRule(normalize(request.fileNameRule()));
        supplier.setS3Bucket(normalize(request.s3Bucket()));
        supplier.setCreator(CurrentUser.required().username());
        supplier.setModifier(CurrentUser.required().username());
        supplier.setCreateTime(LocalDateTime.now());
        supplier.setUpdateTime(LocalDateTime.now());
        supplierMapper.insert(supplier);
        auditService.record("supplier.create", "SUPPLIER", String.valueOf(supplier.getId()), supplierCode);
        return toResponse(supplierMapper.selectById(supplier.getId()));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierUpdateRequest request) {
        permissionEvaluator.require("supplier:write");
        Supplier existing = requireSupplier(id);
        String supplierCode = request.supplierCode().trim();
        assertUniqueCode(supplierCode, id);
        validate(request.fetcherType(), request.updateFrequency());
        supplierMapper.update(
                null,
                new LambdaUpdateWrapper<Supplier>()
                        .eq(Supplier::getId, id)
                        .set(Supplier::getSupplierCode, supplierCode)
                        .set(Supplier::getSupplierName, request.supplierName().trim())
                        .set(Supplier::getFetcherType, request.fetcherType().trim())
                        .set(Supplier::getRemoteSubDir, normalizeRemoteSubDir(request.fetcherType(), request.remoteSubDir()))
                        .set(Supplier::getUpdateFrequency, request.updateFrequency())
                        .set(Supplier::getFileNameRule, normalize(request.fileNameRule()))
                        .set(Supplier::getS3Bucket, normalize(request.s3Bucket()))
                        .set(Supplier::getModifier, CurrentUser.required().username())
                        .set(Supplier::getUpdateTime, LocalDateTime.now())
        );
        auditService.record("supplier.update", "SUPPLIER", String.valueOf(id), existing.getSupplierCode());
        return toResponse(supplierMapper.selectById(id));
    }

    @Transactional
    public SupplierResponse setEnabled(Long id, boolean enabled) {
        permissionEvaluator.require("supplier:write");
        Supplier existing = requireSupplier(id);
        supplierMapper.update(
                null,
                new LambdaUpdateWrapper<Supplier>()
                        .eq(Supplier::getId, id)
                        .set(Supplier::getEnabled, enabled)
                        .set(Supplier::getModifier, CurrentUser.required().username())
                        .set(Supplier::getUpdateTime, LocalDateTime.now())
        );
        auditService.record(
                enabled ? "supplier.enable" : "supplier.disable",
                "SUPPLIER",
                String.valueOf(id),
                existing.getSupplierCode()
        );
        return toResponse(supplierMapper.selectById(id));
    }

    private Supplier requireSupplier(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "供应商不存在");
        }
        return supplier;
    }

    private void assertUniqueCode(String supplierCode, Long excludedId) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getSupplierCode, supplierCode);
        if (excludedId != null) {
            wrapper.ne(Supplier::getId, excludedId);
        }
        if (supplierMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.SUPPLIER_CODE_EXISTS);
        }
    }

    private void validate(String fetcherType, Integer updateFrequency) {
        if (fetcherType == null || !FETCHER_TYPES.contains(fetcherType.trim())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件获取类型只允许 SFTP 或 REST");
        }
        if (updateFrequency != null && updateFrequency < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传频率必须大于等于 1 分钟");
        }
    }

    private String normalizeRemoteSubDir(String fetcherType, String remoteSubDir) {
        return "REST".equals(fetcherType == null ? null : fetcherType.trim())
                ? null
                : normalize(remoteSubDir);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getSupplierCode(),
                supplier.getSupplierName(),
                supplier.getFetcherType(),
                supplier.getRemoteSubDir(),
                Boolean.TRUE.equals(supplier.getEnabled()),
                supplier.getUpdateFrequency(),
                supplier.getFileNameRule(),
                supplier.getS3Bucket(),
                supplier.getCreator(),
                supplier.getCreateTime(),
                supplier.getModifier(),
                supplier.getUpdateTime()
        );
    }
}
