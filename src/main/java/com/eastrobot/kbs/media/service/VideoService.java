package com.eastrobot.kbs.media.service;

import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.aitype.VAC;

import java.util.Map;

/**
 * VideoService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 10:15
 */
public interface VideoService {

    /**
     * 解析视频
     *
     * @param videoFilePath 视频资源文件路径
     * @param paramMap      参数列表
     *
     * @author Yogurt_lei
     * @date 2018-03-27 11:55
     */
    ParseResult<VAC> handle(String videoFilePath, Map paramMap);
}
