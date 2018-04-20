package com.eastrobot.converter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VacParseResult 视频解析结果
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-09 15:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacParseResult extends AbstractParseResult {
    private ParseResult asrParseResult;

    private ParseResult ocrParseResult;
}
