package com.edm.supplier;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("EXTERNAL_SUPPLIER")
public class Supplier {

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("SUPPLIER_CODE")
    private String supplierCode;

    @TableField("SUPPLIER_NAME")
    private String supplierName;

    @TableField("FETCHER_TYPE")
    private String fetcherType;

    @TableField("REMOTE_SUB_DIR")
    private String remoteSubDir;

    @TableField("IS_ENABLE")
    private Boolean enabled;

    @TableField("UPDATE_FREQUENCY")
    private Integer updateFrequency;

    @TableField("FILE_NAME_RULE")
    private String fileNameRule;

    @TableField("S3_BUCKET")
    private String s3Bucket;

    @TableField("CREATOR")
    private String creator;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("MODIFIER")
    private String modifier;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getFetcherType() {
        return fetcherType;
    }

    public void setFetcherType(String fetcherType) {
        this.fetcherType = fetcherType;
    }

    public String getRemoteSubDir() {
        return remoteSubDir;
    }

    public void setRemoteSubDir(String remoteSubDir) {
        this.remoteSubDir = remoteSubDir;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(Integer updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public String getFileNameRule() {
        return fileNameRule;
    }

    public void setFileNameRule(String fileNameRule) {
        this.fileNameRule = fileNameRule;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
