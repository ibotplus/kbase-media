package com.eastrobot.converter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * OcrParseResult
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 16:20
 */
@Data
@AllArgsConstructor
public class OcrParseResult {
    private int code;

    private String result;
}
