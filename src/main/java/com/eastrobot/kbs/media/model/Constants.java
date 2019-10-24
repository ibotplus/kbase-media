package com.eastrobot.kbs.media.model;


/**
 * Global Constants
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 12:47
 */
public interface Constants {
    /**
     * 是否异步解析
     */
    String IS_ASYNC_PARSE = "IS_ASYNC_PARSE";
    //------------------------------------
    // ocr supplier param
    //------------------------------------
    String YOUTU = "youtu";
    String ABBYY = "abbyy";
    String TESSERACT = "tesseract";
    //------------------------------------
    // asr supplier param
    //------------------------------------
    String BAIDU = "baidu";
    String SHHAN = "shhan";
    String XFYUN = "xfyun";
    //------------------------------------
    // tts supplier param
    //------------------------------------
    String M2 = "m2";
    String DATA_BAKER = "baker";
    //------------------------------------
    // AiRecognition request param constants
    //------------------------------------
    /**
     * ai识别目标文件路径
     */
    String AI_RESOURCE_FILE_PATH = "resourceFilePath";
    /**
     * ai识别视频文件是否按帧提取关键字
     */
    String AI_WHETHER_EACH_IMAGE_EXTRACT_KEYWORD = "whetherEachImageExtractKeyword";
    /**
     * 视频文件是否需要预览图
     */
    String AI_WHETHER_NEED_VIDEO_POSTER = "whetherNeedVideoPoster";
    /**
     * AiType see {@link com.eastrobot.kbs.media.model.AiType}
     */
    String AI_TYPE = "AI_TYPE";
    String AI_TTS_TEXT = "AI_TTS_TEXT";
    String AI_TTS_OPTION = "AI_TTS_OPTION";
    //------------------------------------
    // RocketMQ default constants
    //------------------------------------
    String MQ_CREATE_FILE_TOPIC = "MQ_CREATE_FILE_TOPIC";
    String MQ_CREATE_FILE_TAG = "MQ_CREATE_FILE_TAG";
    Integer MQ_RETRY_CONSUME = 5;
    Integer MQ_RETRY_PRODUCT = 5;
}
