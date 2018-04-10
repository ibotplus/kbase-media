package com.eastrobot.converter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AsrParseResult
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-09 15:09
 */
@Data
@AllArgsConstructor
public class AsrParseResult {
    /**
     * 错误码
     */
    private ResultCode code;
    /**
     * 错误信息
     */
    private String message;
    /**
     * 分段部分的正常结果
     */
    private String result;
}
