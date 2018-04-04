package com.eastrobot.converter.model;

import lombok.Getter;
import lombok.Setter;

public enum ErrorCode {
    SUCCESS(0, "SUCCESS"),
    FAILURE(1, "请求失败"),
    SYSTEM_ERROR(1, "系统错误"),
    SYSTEM_BUSY(2, "系统繁忙"),
    PARAM_ERROR(3, "参数错误"),
    CFG_ERROR(4, "配置文件错误"),
    FILE_UPLOAD_FAILED(5, "上传失败"),
    UPDATE_CACHE_ERROR(6, "更新缓存失败"),
    ILLEGAL_OPERATION(7, "操作不合法"),;

    @Getter
    @Setter
    private int code;
    @Getter
    @Setter
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
