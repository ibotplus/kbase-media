package com.eastrobot.converter.model;


/**
 * Global Constants
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 12:47
 */
public interface Constants {
    //------------------------------------
    // ocr-converter supplier param
    //------------------------------------
    String YOUTU = "youtu";
    String ABBYY = "abbyy";
    String TESSERACT = "tesseract";
    //------------------------------------
    // asr-converter supplier param
    //------------------------------------
    String BAIDU = "baidu";
    String SHHAN = "shhan";
    String XFYUN = "xfyun";
    //------------------------------------
    // parse file type definition
    //------------------------------------
    String AUDIO = "AUDIO";
    String IMAGE = "IMAGE";
    String VIDEO = "VIDEO";
    String TEXT = "TEXT";
    //------------------------------------
    // parse result key definition
    //------------------------------------
    String AUDIO_CONTENT = "AUDIO_CONTENT";
    String IMAGE_CONTENT = "IMAGE_CONTENT";
    String AUDIO_KEYWORD = "AUDIO_KEYWORD";
    String IMAGE_KEYWORD = "IMAGE_KEYWORD";
    String ERROR_MSG = "ERROR_MSG";
    //------------------------------------
    // RocketMQ default constants
    //------------------------------------
    String MQ_CREATE_FILE_TOPIC = "MQ_CREATE_FILE_TOPIC";
    String MQ_CREATE_FILE_TAG = "MQ_CREATE_FILE_TAG";
    Integer MQ_RETRY_CONSUME = 5;
    Integer MQ_RETRY_PRODUCT = 5;
}
