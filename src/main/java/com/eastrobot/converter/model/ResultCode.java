package com.eastrobot.converter.model;

/**
 * 定义解析结果码
 */
public enum ResultCode {
    SUCCESS(0, "SUCCESS"),
    FAILURE(101, "请求失败"),
    PARAM_ERROR(102, "参数错误"),
    CFG_ERROR(103, "配置文件错误"),
    FILE_UPLOAD_FAILED(104, "文件上传失败"),
    ILLEGAL_TYPE(105, "文件类型不支持"),
    PARSE_EMPTY(106, "解析内容为空"),
    OCR_FAILURE(107, "图片解析失败"),
    ASR_FAILURE(108, "音频解析失败"),
    PART_PARSE_FAILED(109, "图片或音频部分解析失败"),
    PREPARE_UPLOAD_FILE_ERROR(110, "预处理上传文件发生异常."),
    TTS_FAILURE(111, "文本合成语音失败"),
    ASYNC_NOT_COMPLETED(201, "解析还在进行中,请稍后尝试读取结果."),
    ASYNC_READ_RESULT_FILE_FAILED(202, "读取解析结果文件失败.");

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
