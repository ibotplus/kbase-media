package com.eastrobot.kbs.media.service;

import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.aitype.OCR;

import java.util.Map;

/**
 * ImageService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 15:14
 */
public interface ImageService {
    /**
     * 解析图片
     *
     * @param imageFilePath 图片文件or存放图片文件夹(用于视频帧解析)
     * @param paramMap      参数列表
     */
    ParseResult<OCR> handle(String imageFilePath, Map paramMap);
}
