package com.eastrobot.converter.model;

/**
 * 定义错误码
 */
public enum ErrorCode {
    SUCCESS(0, "SUCCESS"),
    FAILURE(1, "请求失败"),
    SYSTEM_BUSY(2, "系统繁忙"),
    PARAM_ERROR(3, "参数错误"),
    CFG_ERROR(4, "配置文件错误"),
    FILE_UPLOAD_FAILED(5, "上传失败"),
    UPDATE_CACHE_ERROR(6, "更新缓存失败"),
    ILLEGAL_OPERATION(7, "操作不合法"),;

    private int code;
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
