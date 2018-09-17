package com.eastrobot.kbs.media.service;


import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.aitype.ASR;
import com.eastrobot.kbs.media.model.aitype.TTS;

import java.util.Map;

/**
 * AudioService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 15:14
 */
public interface AudioService {

    /**
     * 解析音频(pcm格式) 生成文本
     *
     * @author Yogurt_lei
     * @date 2018-03-27 11:54
     */
    ParseResult<ASR> handle(String audioFilePath);

    /**
     * 文本合成音频（byte[]输出）
     */
    ParseResult<TTS> handleTts(String text, Map ttsOption);
}
