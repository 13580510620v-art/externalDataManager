package com.edm.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("SYS_USER")
public class User {

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("USERNAME")
    private String username;

    @TableField("PASSWORD_HASH")
    private String passwordHash;

    @TableField("FULL_NAME")
    private String fullName;

    @TableField("EMAIL")
    private String email;

    @TableField("SAML_NAME_ID")
    private String samlNameId;

    @TableField("IS_ENABLE")
    private Boolean enabled;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSamlNameId() {
        return samlNameId;
    }

    public void setSamlNameId(String samlNameId) {
        this.samlNameId = samlNameId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
