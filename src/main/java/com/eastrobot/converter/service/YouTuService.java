package com.eastrobot.converter.service;


public interface YouTuService {

    /**
     * 识别图片中的文字
     *
     * @param imagePath 图片物理地址
     */
    String ocr(String imagePath) throws Exception;
}
