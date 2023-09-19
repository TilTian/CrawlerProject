package org.example.grmsapi;

public enum ResultCode implements IErrorCode {
    SUCCESS("success", "API调用成功"),
    FAILED("fail", "API调用失败"),
    VALIDATE_FAILED("fail", "参数检验失败"),
    UNAUTHORIZED("unauthorized", "暂未登录或token已经过期"),
    FORBIDDEN("forbidden", "没有相关权限"),
    WARN("warn", "WARNING"),
    SHOWSUCCESS("showSuccess","API调用成功，提示用户");
    private String code;
    private String message;

    private ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}