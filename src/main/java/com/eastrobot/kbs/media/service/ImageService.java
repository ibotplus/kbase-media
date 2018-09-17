package com.eastrobot.kbs.media.service;

import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.aitype.OCR;

/**
 * ImageService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 15:14
 */
public interface ImageService {
    /**
     * 解析图片 生成文本
     */
    ParseResult<OCR> handle(String imageFilePath);

    /**
     * 解析文件夹下图片 生成文本
     *
     * @param folder                    文件夹
     * @param eachPictureExtractKeyword 每张图片都提取关键字合并 还是最终结果合并后提取关键字
     */
    ParseResult<OCR> handleMultiFiles(String folder, boolean eachPictureExtractKeyword);
}
