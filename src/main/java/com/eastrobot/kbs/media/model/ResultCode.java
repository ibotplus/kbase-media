package com.eastrobot.kbs.media.model;

/**
 * 定义解析结果码
 *
 * @author Yogurt_lei
 * @date 2018-05-02 15:33
 */
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS(0, "SUCCESS"),
    /**
     * 请求失败
     */
    FAILURE(101, "请求失败"),
    /**
     * 参数错误
     */
    PARAM_ERROR(102, "参数错误"),
    /**
     * 配置错误
     */
    CFG_ERROR(103, "配置错误"),
    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILURE(104, "文件上传失败"),
    /**
     * 文件类型不支持
     */
    ILLEGAL_TYPE(105, "文件类型不支持"),
    /**
     * 解析内容为空
     */
    PARSE_EMPTY(106, "解析内容为空"),
    /**
     * 图片解析失败
     */
    OCR_FAILURE(107, "图片解析失败"),
    /**
     * 音频解析失败
     */
    ASR_FAILURE(108, "音频解析失败"),
    /**
     * 图片或音频部分解析失败
     */
    PART_PARSE_FAILURE(109, "图片或音频部分解析失败"),
    /**
     * TTS失败
     */
    TTS_FAILURE(110, "文本合成语音失败"),
    /**
     * 解析未完成
     */
    NOT_COMPLETED(201, "解析未完成."),
    /**
     * 结果文件读取失败
     */
    READ_RESULT_FILE_FAILURE(202, "读取解析结果文件失败.");

    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }
}
