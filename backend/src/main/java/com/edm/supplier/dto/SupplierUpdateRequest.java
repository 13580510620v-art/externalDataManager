package com.edm.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "更新供应商请求")
public record SupplierUpdateRequest(
        @Schema(description = "供应商编码") @NotBlank(message = "供应商编码不能为空") String supplierCode,
        @Schema(description = "供应商名称") @NotBlank(message = "供应商名称不能为空") String supplierName,
        @Schema(description = "文件获取类型") @NotBlank(message = "文件获取类型不能为空") String fetcherType,
        @Schema(description = "远端子目录") String remoteSubDir,
        @Schema(description = "上传频率，单位分钟") Integer updateFrequency,
        @Schema(description = "文件命名规则") String fileNameRule,
        @Schema(description = "S3 Bucket") String s3Bucket
) {
}
