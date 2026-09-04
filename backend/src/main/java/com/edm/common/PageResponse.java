package com.edm.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "分页响应")
public record PageResponse<T>(
        @Schema(description = "当前页数据") List<T> records,
        @Schema(description = "当前页码") long page,
        @Schema(description = "每页数量") long size,
        @Schema(description = "总记录数") long total,
        @Schema(description = "总页数") long totalPages
) {

    public static <T> PageResponse<T> of(IPage<T> page) {
        return new PageResponse<>(
                page.getRecords(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages()
        );
    }
}
