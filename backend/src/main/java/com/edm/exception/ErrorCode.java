package com.edm.exception;

public enum ErrorCode {
    BAD_REQUEST(400, "请求参数不正确"),
    UNAUTHORIZED(401, "登录状态已失效"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "系统内部错误"),
    LOGIN_FAILED(4001, "用户名或密码错误"),
    SAML_NOT_ENABLED(4002, "SAML 登录未启用"),
    SAML_USER_NOT_FOUND(4003, "SAML 用户未预建，请联系管理员"),
    SUPPLIER_CODE_EXISTS(4004, "供应商编码已存在"),
    USERNAME_EXISTS(4005, "用户名已存在"),
    EMAIL_EXISTS(4006, "邮箱已存在"),
    GROUP_CODE_EXISTS(4007, "群组编码已存在"),
    TASK_RETRY_NOT_ALLOWED(4008, "当前任务状态不允许重试");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
