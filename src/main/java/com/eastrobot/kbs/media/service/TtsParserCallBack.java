package com.eastrobot.kbs.media.service;

import java.util.Map;

/**
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2019-06-06 11:24
 */
@FunctionalInterface
public interface TtsParserCallBack {
    String doInParser(final String text, final Map<String, Object> ttsOption) throws Exception;

}
