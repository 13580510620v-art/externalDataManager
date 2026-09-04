package com.edm.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("DATA_TASK")
public class DataTask {

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("SUPPLIER_CODE")
    private String supplierCode;

    @TableField("SOURCE_UNIQUE_KEY")
    private String sourceUniqueKey;

    @TableField("SOURCE_FILE_NAME")
    private String sourceFileName;

    @TableField("SOURCE_REMOTE_PATH")
    private String sourceRemotePath;

    @TableField("SOURCE_FILE_SIZE")
    private Long sourceFileSize;

    @TableField("SOURCE_MTIME")
    private Long sourceMtime;

    @TableField("FETCHER_TYPE")
    private String fetcherType;

    @TableField("STATUS")
    private String status;

    @TableField("LOCAL_TEMP_PATH")
    private String localTempPath;

    @TableField("DOWNLOAD_RETRY_TIMES")
    private Integer downloadRetryTimes;

    @TableField("UPDATE_RETRY_TIMES")
    private Integer updateRetryTimes;

    @TableField("INFORM_RETRY_TIMES")
    private Integer informRetryTimes;

    @TableField("S3_BUCKET")
    private String s3Bucket;

    @TableField("TARGET_S3_KEY")
    private String targetS3Key;

    @TableField("INFORM_TIME")
    private LocalDateTime informTime;

    @TableField("FEEDBACK_FLAG")
    private String feedbackFlag;

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

    public String getSourceUniqueKey() {
        return sourceUniqueKey;
    }

    public void setSourceUniqueKey(String sourceUniqueKey) {
        this.sourceUniqueKey = sourceUniqueKey;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getSourceRemotePath() {
        return sourceRemotePath;
    }

    public void setSourceRemotePath(String sourceRemotePath) {
        this.sourceRemotePath = sourceRemotePath;
    }

    public Long getSourceFileSize() {
        return sourceFileSize;
    }

    public void setSourceFileSize(Long sourceFileSize) {
        this.sourceFileSize = sourceFileSize;
    }

    public Long getSourceMtime() {
        return sourceMtime;
    }

    public void setSourceMtime(Long sourceMtime) {
        this.sourceMtime = sourceMtime;
    }

    public String getFetcherType() {
        return fetcherType;
    }

    public void setFetcherType(String fetcherType) {
        this.fetcherType = fetcherType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocalTempPath() {
        return localTempPath;
    }

    public void setLocalTempPath(String localTempPath) {
        this.localTempPath = localTempPath;
    }

    public Integer getDownloadRetryTimes() {
        return downloadRetryTimes;
    }

    public void setDownloadRetryTimes(Integer downloadRetryTimes) {
        this.downloadRetryTimes = downloadRetryTimes;
    }

    public Integer getUpdateRetryTimes() {
        return updateRetryTimes;
    }

    public void setUpdateRetryTimes(Integer updateRetryTimes) {
        this.updateRetryTimes = updateRetryTimes;
    }

    public Integer getInformRetryTimes() {
        return informRetryTimes;
    }

    public void setInformRetryTimes(Integer informRetryTimes) {
        this.informRetryTimes = informRetryTimes;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getTargetS3Key() {
        return targetS3Key;
    }

    public void setTargetS3Key(String targetS3Key) {
        this.targetS3Key = targetS3Key;
    }

    public LocalDateTime getInformTime() {
        return informTime;
    }

    public void setInformTime(LocalDateTime informTime) {
        this.informTime = informTime;
    }

    public String getFeedbackFlag() {
        return feedbackFlag;
    }

    public void setFeedbackFlag(String feedbackFlag) {
        this.feedbackFlag = feedbackFlag;
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
