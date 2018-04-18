package com.eastrobot.converter.model;

import com.eastrobot.converter.config.ConvertConfig;

/**
 * Constants
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 12:47
 */
public interface Constants {
    //------------------------------------
    //result support type
    //------------------------------------
    String KEYWORD = "keyword";
    String FULLTEXT = "fulltext";
    
    /**
     * @see ConvertConfig#getDefaultVideoConfig()
     */
    String VCA = "vca";

    String DEFAULT_TOOL = "default";

    /**
     * @see ConvertConfig#getDefaultImageConfig()
     */
    String OCR = "ocr";
    //------------------------------------
    // ocr-converter supplier param
    //------------------------------------
    String YOUTU = "youtu";

    /**
     * @see ConvertConfig#getDefaultAudioConfig()
     */
    String ASR = "asr";
    //------------------------------------
    // asr-converter supplier param
    //------------------------------------
    String BAIDU = "baidu";
    String SHHAN = "shhan";

    //------------------------------------
    // String Blank result
    //------------------------------------
    /**
     * EMPTY_RESULT
     */
    String EMPTY = "";
    //------------------------------------
    // parse file type key definition
    //------------------------------------
    String AUDIO = "AUDIO";
    String IMAGE = "IMAGE";
    String VIDEO = "VIDEO";
    //------------------------------------
    // parse result key definition
    //------------------------------------
    String AUDIO_CONTENT = "AUDIO_CONTENT";
    String IMAGE_CONTENT = "IMAGE_CONTENT";
    String AUDIO_KEYWORD = "AUDIO_KEYWORD";
    String IMAGE_KEYWORD = "IMAGE_KEYWORD";
    String ERROR_MSG = "ERROR_MSG";

    //------------------------------------
    // parse result file extension
    //------------------------------------
    String RESULT_FILE_EXTENSION = "rs";
    String RESULT_FILE_EXTENSION_WITH_POINT = ".rs";

    //------------------------------------
    // RocketMQ default constants
    //------------------------------------
    String MQ_CREATE_FILE_TOPIC = "MQ_CREATE_FILE_TOPIC";
    String MQ_CREATE_FILE_TAG = "MQ_CREATE_FILE_TAG";
    Integer MQ_RETRY_CONSUME = 5;
    Integer MQ_RETRY_PRODUCT = 5;
}
