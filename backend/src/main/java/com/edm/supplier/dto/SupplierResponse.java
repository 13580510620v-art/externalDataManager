package com.edm.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "供应商信息")
public record SupplierResponse(
        @Schema(description = "供应商 ID") Long id,
        @Schema(description = "供应商编码") String supplierCode,
        @Schema(description = "供应商名称") String supplierName,
        @Schema(description = "文件获取类型") String fetcherType,
        @Schema(description = "远端子目录") String remoteSubDir,
        @Schema(description = "是否启用") boolean enabled,
        @Schema(description = "上传频率") Integer updateFrequency,
        @Schema(description = "文件命名规则") String fileNameRule,
        @Schema(description = "S3 Bucket") String s3Bucket,
        @Schema(description = "创建人") String creator,
        @Schema(description = "创建时间") LocalDateTime createTime,
        @Schema(description = "更新人") String modifier,
        @Schema(description = "更新时间") LocalDateTime updateTime
) {
}
