package com.eastrobot.converter.util;


import com.baidu.aip.speech.AipSpeech;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaiduSpeechUtils {

    private static final Logger log = LoggerFactory.getLogger(BaiduSpeechUtils.class);

    private static final String APP_ID = PropertiesUtil.getString("convert.tools.audio.asr.baidu.appId");
    private static final String API_KEY = PropertiesUtil.getString("convert.tools.audio.asr.baidu.apiKey");
    private static final String SECRET_KEY = PropertiesUtil.getString("convert.tools.audio.asr.baidu.secretKey");

    /*
     * AipSpeech是语音识别的Java客户端，为使用语音识别的开发人员提供了一系列的交互方法。
     * 采用单列初始化AipSpeech，避免重复获取access_token：
     * */
    private static AipSpeech client;

    static {
        if (client == null) {
            client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
        }
    }

    /**
     * @param path   语音文件所在路径
     * @param format 语音文件的格式 包括pcm（不压缩）、wav、opus、speex、amr 不区分大小写
     * @param rate   采样率，支持 8000 或者 16000
     */
    public static JSONObject asr(String path, String format, int rate) {
        return client.asr(path, format, rate, null);
    }

    /**
     * @param data   语音文件二进制数据
     * @param format 语音文件的格式 包括pcm（不压缩）、wav、opus、speex、amr 不区分大小写
     * @param rate   采样率，支持 8000 或者 16000
     */
    public static JSONObject asr(byte[] data, String format, int rate) {
        return client.asr(data, format, rate, null);
    }

}
