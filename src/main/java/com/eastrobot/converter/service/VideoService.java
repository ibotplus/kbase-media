package com.eastrobot.converter.service;

import com.eastrobot.converter.model.VacParseResult;

/**
 * VideoService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 10:15
 */
public interface VideoService {

    /**
     *
     * 解析视频 生成文本
     *
     * @author Yogurt_lei
     * @date 2018-03-27 11:55
     */
    VacParseResult handle(String videoFilePath, boolean isFrameExtractKeyword);
}
