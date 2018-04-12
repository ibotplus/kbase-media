package com.eastrobot.converter.model;

/**
 * 定义解析结果码
 */
public enum ResultCode {
    SUCCESS(0, "SUCCESS"),
    FAILURE(1, "请求失败"),
    SYSTEM_BUSY(2, "系统繁忙"),
    PARAM_ERROR(3, "参数错误"),
    CFG_ERROR(4, "配置文件错误"),
    FILE_UPLOAD_FAILED(5, "上传失败"),
    UPDATE_CACHE_ERROR(6, "更新缓存失败"),
    ILLEGAL_TYPE(7, "文件类型不支持"),
    PARSE_EMPTY(8, "解析内容为空"),
    OCR_FAILURE(9, "OCR解析错误"),
    FILE_UPLOAD_SUCCESS(10, "文件上传成功,通过sn来获取解析结果."),
    ASR_PART_PARSE_FAILED(11, "语音解析部分失败."),
    OCR_PART_PARSE_FAILED(12, "图片解析部分失败.");

    private int code;
    private String msg;

    ResultCode(int code, String msg) {
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
