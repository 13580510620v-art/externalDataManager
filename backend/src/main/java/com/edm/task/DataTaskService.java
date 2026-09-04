package com.edm.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import com.edm.security.CurrentUser;
import com.edm.security.PermissionEvaluator;
import com.edm.system.AuditService;
import com.edm.task.dto.DataTaskDetailResponse;
import com.edm.task.dto.DataTaskQuery;
import com.edm.task.dto.DataTaskResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

@Service
public class DataTaskService {

    private static final Map<String, Function<DataTask, Integer>> RETRY_FIELD = Map.of(
            "DOWNLOAD_FAILED", DataTask::getDownloadRetryTimes,
            "UPLOAD_FAILED", DataTask::getUpdateRetryTimes,
            "INFORM_FAILED", DataTask::getInformRetryTimes
    );

    private final DataTaskMapper dataTaskMapper;
    private final PermissionEvaluator permissionEvaluator;
    private final AuditService auditService;

    public DataTaskService(
            DataTaskMapper dataTaskMapper,
            PermissionEvaluator permissionEvaluator,
            AuditService auditService
    ) {
        this.dataTaskMapper = dataTaskMapper;
        this.permissionEvaluator = permissionEvaluator;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public IPage<DataTaskResponse> page(DataTaskQuery query) {
        permissionEvaluator.require("task:read");
        long currentPage = Math.max(query.page(), 1);
        long pageSize = query.size() <= 0 ? 10 : Math.min(query.size(), 100);
        LambdaQueryWrapper<DataTask> wrapper = new LambdaQueryWrapper<>();
        if (query.supplierCode() != null && !query.supplierCode().isBlank()) {
            wrapper.eq(DataTask::getSupplierCode, query.supplierCode().trim());
        }
        if (query.status() != null && !query.status().isBlank()) {
            wrapper.eq(DataTask::getStatus, query.status().trim());
        }
        if (query.fileName() != null && !query.fileName().isBlank()) {
            wrapper.like(DataTask::getSourceFileName, query.fileName().trim());
        }
        if (query.feedbackFlag() != null && !query.feedbackFlag().isBlank()) {
            wrapper.eq(DataTask::getFeedbackFlag, query.feedbackFlag().trim());
        }
        if (query.startTime() != null) {
            wrapper.ge(DataTask::getCreateTime, query.startTime());
        }
        if (query.endTime() != null) {
            wrapper.le(DataTask::getCreateTime, query.endTime());
        }
        wrapper.orderByDesc(DataTask::getCreateTime).orderByDesc(DataTask::getId);
        Page<DataTask> result = dataTaskMapper.selectPage(new Page<>(currentPage, pageSize), wrapper);
        Page<DataTaskResponse> responsePage = new Page<>(currentPage, pageSize, result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(this::toResponse).toList());
        return responsePage;
    }

    @Transactional(readOnly = true)
    public DataTaskDetailResponse detail(Long id) {
        permissionEvaluator.require("task:read");
        return toDetailResponse(requireTask(id));
    }

    @Transactional
    public DataTaskResponse retry(Long id) {
        permissionEvaluator.require("task:retry");
        DataTask task = requireTask(id);
        Function<DataTask, Integer> retryTimesGetter = RETRY_FIELD.get(task.getStatus());
        if (retryTimesGetter == null) {
            throw new BusinessException(ErrorCode.TASK_RETRY_NOT_ALLOWED);
        }
        int retryTimes = retryTimesGetter.apply(task) == null ? 0 : retryTimesGetter.apply(task);
        LambdaUpdateWrapper<DataTask> wrapper = new LambdaUpdateWrapper<DataTask>()
                .eq(DataTask::getId, id)
                .eq(DataTask::getStatus, task.getStatus())
                .set(DataTask::getStatus, "PENDING")
                .set(DataTask::getModifier, CurrentUser.required().username())
                .set(DataTask::getUpdateTime, LocalDateTime.now());
        if ("DOWNLOAD_FAILED".equals(task.getStatus())) {
            wrapper.set(DataTask::getDownloadRetryTimes, retryTimes + 1);
        } else if ("UPLOAD_FAILED".equals(task.getStatus())) {
            wrapper.set(DataTask::getUpdateRetryTimes, retryTimes + 1);
        } else {
            wrapper.set(DataTask::getInformRetryTimes, retryTimes + 1);
        }
        if (dataTaskMapper.update(null, wrapper) != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "任务状态已变化，请刷新后重试");
        }
        auditService.record("task.retry", "DATA_TASK", String.valueOf(id), task.getStatus());
        return toResponse(requireTask(id));
    }

    private DataTask requireTask(Long id) {
        DataTask task = dataTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "数据任务不存在");
        }
        return task;
    }

    private DataTaskResponse toResponse(DataTask task) {
        return new DataTaskResponse(
                task.getId(),
                task.getSupplierCode(),
                task.getSourceUniqueKey(),
                task.getSourceFileName(),
                task.getSourceRemotePath(),
                task.getSourceFileSize(),
                task.getSourceMtime(),
                task.getFetcherType(),
                task.getStatus(),
                value(task.getDownloadRetryTimes()),
                value(task.getUpdateRetryTimes()),
                value(task.getInformRetryTimes()),
                task.getS3Bucket(),
                task.getTargetS3Key(),
                task.getInformTime(),
                task.getFeedbackFlag(),
                task.getCreateTime(),
                task.getUpdateTime()
        );
    }

    private DataTaskDetailResponse toDetailResponse(DataTask task) {
        return new DataTaskDetailResponse(
                task.getId(),
                task.getSupplierCode(),
                task.getSourceUniqueKey(),
                task.getSourceFileName(),
                task.getSourceRemotePath(),
                task.getSourceFileSize(),
                task.getSourceMtime(),
                task.getFetcherType(),
                task.getStatus(),
                task.getLocalTempPath(),
                value(task.getDownloadRetryTimes()),
                value(task.getUpdateRetryTimes()),
                value(task.getInformRetryTimes()),
                task.getS3Bucket(),
                task.getTargetS3Key(),
                task.getInformTime(),
                task.getFeedbackFlag(),
                task.getCreator(),
                task.getCreateTime(),
                task.getModifier(),
                task.getUpdateTime()
        );
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }
}
