package com.eastrobot.converter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ParseResult
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-20 17:56
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParseResult extends AbstractParseResult {
    /**
     * 错误码
     */
    private ResultCode code;
    /**
     * 错误信息
     */
    private String message;
    /**
     * 关键字
     */
    private String keyword;
    /**
     * 内容
     */
    private String content;

    /**
     * 音频
     */
    private byte[] audio;


    public void update(ParseResult other) {
        this.code = other.code;
        this.message = other.message;
        this.keyword = other.keyword;
        this.content = other.content;
        this.audio = other.audio;
    }
}
