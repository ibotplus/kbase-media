package com.eastrobot.converter.service;

/**
 * AudioParserCallBack
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-16 15:12
 */
public interface AudioParserCallBack {
    String doInAudioParser(final String fileName) throws Exception;
}
