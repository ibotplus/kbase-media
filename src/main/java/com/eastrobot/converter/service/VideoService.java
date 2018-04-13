package com.eastrobot.converter.service;


import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.model.AsrParseResult;

/**
 * VideoService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 10:15
 */
public interface VideoService {

    AsrParseResult handle(String videoFilePath);

    /**
     *
     * 解析视频
     * @param videoPath 视频绝对路径
     * @return 视频解析结果
     *
     * @author Yogurt_lei
     * @date 2018-03-26 10:16
     */
    JSONObject parseVideo(String videoPath);
}
