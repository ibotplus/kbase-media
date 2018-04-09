package com.eastrobot.converter.util.baidu;

/**
 * BaiduAsrConstants
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-09 16:25
 */
public interface BaiduAsrConstants {

    //------------------------------------
    // asr dev_pid param
    //------------------------------------
    /**
     * 普通话(支持简单的英文识别) 搜索模型 无标点 (默认选项)
     */
    String PID_1536 = "1536";
    /**
     * 普通话(纯中文识别)	输入法模型 有标点
     */
    String PID_1537 = "1537";
    /**
     * 粤语 有标点
     */
    String PID_1637 = "1637";
    /**
     * 英语 有标点
     */
    String PID_1737 = "1737";
    /**
     * 四川话 有标点
     */
    String PID_1837 = "1837";
    /**
     * 普通话远场 远场模型	 有标点
     */
    String PID_1936 = "1936";

    /**
     * 百度上传别的去解析都会先转成pcm 不如自己转pcm再去调
     */
    String PCM = "pcm";
    /**
     * 默认音频采样率
     */
    int RATE = 16000;
    /**
     * 音频最大长度 (单位:s)
     */
    int MAX_DURATION = 60;

    //------------------------------------
    // asr return errorCode
    //------------------------------------
    enum errorCode {
        ERROR_3300(3300, "输入参数不正确"),
        ERROR_3301(3301, "音频质量过差"),
        ERROR_3302(3302, "鉴权失败"),
        ERROR_3303(3303, "语音服务器后端问题"),
        ERROR_3304(3304, "用户的请求QPS超限"),
        ERROR_3305(3305, "用户的日pv（日请求量）超限"),
        ERROR_3307(3307, "语音服务器后端识别出错问题"),
        ERROR_3308(3308, "音频过长"),
        ERROR_3309(3309, "音频数据问题"),
        ERROR_3310(3310, "输入的音频文件过大(>10MB)"),
        ERROR_3311(3311, "采样率rate参数不在选项里"),
        ERROR_3312(3312, "音频格式format参数不在选项里");

        int key;
        String value;

        errorCode(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
