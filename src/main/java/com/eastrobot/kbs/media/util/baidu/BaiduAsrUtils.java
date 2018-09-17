package com.eastrobot.kbs.media.util.baidu;


import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.eastrobot.kbs.media.model.Constants;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * see http://ai.baidu.com/docs#/ASR-Online-Java-SDK/top
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "audio.asr.default", havingValue = Constants.BAIDU)
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
        log.info("initialize baidu asr tools complete.");
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

    /**
     *
     * @param tex 合成的文本，使用UTF-8编码。小于512个中文字或者英文数字。（文本在百度服务器内转换为GBK后，长度必须小于1024字节）
     * @param lan 固定值zh。语言选择,目前只有中英文混合模式，填写固定值zh
     * @param ctp 客户端类型选择，web端填写固定值1
     * @param options 可选参数
     *                spd:语速，取值0-9，默认为5中语速,
     *                pit:音调，取值0-9，默认为5中语调,
     *                vol:音量，取值0-15，默认为5中音量,
     *                per:发音人选择, 0为女声，1为男声，3为情感合成-度逍遥，4为情感合成-度丫丫，默认为普通女
     * @return
     */
    public static byte[] tts(String tex, String lan, int ctp, HashMap<String, Object> options){
        TtsResponse tts = client.synthesis(tex,lan,ctp, options);
        return tts.getData();
    }

}
