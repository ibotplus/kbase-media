package com.eastrobot.converter.service;


import com.eastrobot.converter.model.ParseResult;
import com.eastrobot.converter.model.tts.TTSParam;

/**
 * AudioService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 15:14
 */
public interface AudioService {

    /**
     *
     * 解析音频(pcm格式) 生成文本
     *
     * @author Yogurt_lei
     * @date 2018-03-27 11:54
     */
    ParseResult handle(String audioFilePath);

    /**
     * 文本合成音频（byte[]输出）
     * @param param
     * @return
     */
    ParseResult handleTts(TTSParam param);
}
