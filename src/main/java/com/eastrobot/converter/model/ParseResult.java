package com.eastrobot.converter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ParseResult
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 16:20
 */
@Data
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
     * 分段 or 正常结果
     */
    private String result;

    public void updateResult(ParseResult other) {
        this.code = other.code;
        this.message = other.message;
        this.result = other.result;
    }
}
