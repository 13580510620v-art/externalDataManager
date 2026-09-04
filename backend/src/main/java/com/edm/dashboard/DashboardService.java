package com.edm.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edm.supplier.Supplier;
import com.edm.supplier.SupplierMapper;
import com.edm.security.PermissionEvaluator;
import com.edm.task.DataTask;
import com.edm.task.DataTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final List<String> FAILURE_STATUSES = List.of(
            "DOWNLOAD_FAILED",
            "UPLOAD_FAILED",
            "INFORM_FAILED"
    );

    private final DataTaskMapper dataTaskMapper;
    private final PermissionEvaluator permissionEvaluator;
    private final SupplierMapper supplierMapper;

    public DashboardService(
            DataTaskMapper dataTaskMapper,
            PermissionEvaluator permissionEvaluator,
            SupplierMapper supplierMapper
    ) {
        this.dataTaskMapper = dataTaskMapper;
        this.permissionEvaluator = permissionEvaluator;
        this.supplierMapper = supplierMapper;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse today() {
        permissionEvaluator.require("dashboard:read");
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();
        List<DataTask> tasks = dataTaskMapper.selectList(new LambdaQueryWrapper<DataTask>()
                .ge(DataTask::getCreateTime, startTime)
                .lt(DataTask::getCreateTime, endTime));
        long total = tasks.size();
        long completed = tasks.stream()
                .filter(task -> "COMPLETED".equals(task.getStatus()))
                .count();
        Map<String, Long> statusCounts = tasks.stream()
                .collect(Collectors.groupingBy(
                        DataTask::getStatus,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        Map<String, Long> supplierCounts = tasks.stream()
                .collect(Collectors.groupingBy(
                        DataTask::getSupplierCode,
                        LinkedHashMap::new,
                Collectors.counting()
                ));
        long failureCount = tasks.stream()
                .filter(task -> FAILURE_STATUSES.contains(task.getStatus()))
                .count();
        LocalDateTime lastWeekStart = today.minusWeeks(1).atStartOfDay();
        LocalDateTime lastWeekEnd = today.minusWeeks(1).plusDays(1).atStartOfDay();
        Long lastWeekTotal = dataTaskMapper.selectCount(new LambdaQueryWrapper<DataTask>()
                .ge(DataTask::getCreateTime, lastWeekStart)
                .lt(DataTask::getCreateTime, lastWeekEnd));
        Long lastWeekFailureCount = dataTaskMapper.selectCount(new LambdaQueryWrapper<DataTask>()
                .ge(DataTask::getCreateTime, lastWeekStart)
                .lt(DataTask::getCreateTime, lastWeekEnd)
                .in(DataTask::getStatus, FAILURE_STATUSES));
        Page<DataTask> recentPage = dataTaskMapper.selectPage(new Page<>(1, 8), new LambdaQueryWrapper<DataTask>()
                .orderByDesc(DataTask::getCreateTime)
                .orderByDesc(DataTask::getId));
        Map<String, String> supplierNames = findSupplierNames(recentPage.getRecords());
        return new DashboardSummaryResponse(
                today,
                total,
                completed,
                total == 0 ? 0 : (double) completed / total,
                statusCounts,
                supplierCounts,
                total - lastWeekTotal,
                failureCount - lastWeekFailureCount,
                recentPage.getRecords().stream()
                        .map(task -> new DashboardRecentTaskResponse(
                                task.getId(),
                                supplierNames.getOrDefault(task.getSupplierCode(), task.getSupplierCode()),
                                task.getSourceFileName(),
                                task.getSourceFileSize(),
                                task.getStatus(),
                                task.getCreateTime()
                        ))
                        .toList()
        );
    }

    private Map<String, String> findSupplierNames(List<DataTask> tasks) {
        List<String> supplierCodes = tasks.stream()
                .map(DataTask::getSupplierCode)
                .distinct()
                .toList();
        if (supplierCodes.isEmpty()) {
            return Map.of();
        }
        return supplierMapper.selectList(new LambdaQueryWrapper<Supplier>()
                        .in(Supplier::getSupplierCode, supplierCodes))
                .stream()
                .collect(Collectors.toMap(
                        Supplier::getSupplierCode,
                        Supplier::getSupplierName,
                        (left, right) -> left
                ));
    }
}
