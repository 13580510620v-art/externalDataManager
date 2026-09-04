package com.edm.task;

import com.edm.common.ApiResponse;
import com.edm.common.PageResponse;
import com.edm.task.dto.DataTaskDetailResponse;
import com.edm.task.dto.DataTaskQuery;
import com.edm.task.dto.DataTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "数据任务", description = "外部数据文件任务查询和重试")
@RestController
@RequestMapping("/api/tasks")
public class DataTaskController {

    private final DataTaskService dataTaskService;

    public DataTaskController(DataTaskService dataTaskService) {
        this.dataTaskService = dataTaskService;
    }

    @Operation(summary = "分页查询数据任务")
    @GetMapping
    public ApiResponse<PageResponse<DataTaskResponse>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            String supplierCode,
            String status,
            String fileName,
            String feedbackFlag,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return ApiResponse.success(PageResponse.of(dataTaskService.page(new DataTaskQuery(
                supplierCode,
                status,
                fileName,
                feedbackFlag,
                startTime,
                endTime,
                page,
                size
        ))));
    }

    @Operation(summary = "查询数据任务详情")
    @GetMapping("/{id}")
    public ApiResponse<DataTaskDetailResponse> detail(
            @Parameter(description = "任务 ID") @PathVariable Long id
    ) {
        return ApiResponse.success(dataTaskService.detail(id));
    }

    @Operation(summary = "重试失败任务")
    @PostMapping("/{id}/retry")
    public ApiResponse<DataTaskResponse> retry(
            @Parameter(description = "任务 ID") @PathVariable Long id
    ) {
        return ApiResponse.success(dataTaskService.retry(id));
    }
}
