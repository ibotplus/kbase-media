package com.eastrobot.converter.util.baidu;


import com.baidu.aip.speech.AipSpeech;
import com.eastrobot.converter.model.Constants;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * see http://ai.baidu.com/docs#/ASR-Online-Java-SDK/top
 */
@Component
@ConditionalOnProperty(prefix = "convert", name = "video.asr.default", havingValue = Constants.BAIDU)
public class BaiduAsrUtils {

    @Value("${convert.audio.asr.baidu.appId}")
    private String APP_ID;
    @Value("${convert.audio.asr.baidu.apiKey}")
    private String API_KEY;
    @Value("${convert.audio.asr.baidu.secretKey}")
    private String SECRET_KEY;

    private static AipSpeech client;

    @PostConstruct
    public void init() {
        client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
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
