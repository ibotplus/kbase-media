package com.eastrobot.kbs.media.service;

import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.aitype.TTS;

import java.util.Map;

/**
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2019-06-06 9:10
 */
public interface TtsService {

    /**
     * 文本合成音频（byte[]输出）
     */
    ParseResult<TTS> handle(String text, Map<String, Object> ttsOption);
}
