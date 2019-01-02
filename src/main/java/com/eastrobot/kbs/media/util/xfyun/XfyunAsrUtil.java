package com.eastrobot.kbs.media.util.xfyun;

import com.eastrobot.kbs.media.model.Constants;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * XfyunAsrUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-19 16:09
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "audio.asr.default", havingValue = Constants.XFYUN)
public class XfyunAsrUtil {

    @Value("${convert.audio.asr.xfyun.appId}")
    private String appId;
    @Value("${convert.audio.asr.xfyun.apiKey}")
    private String apiKey;
    @Value("${convert.audio.asr.xfyun.apiUrl}")
    private String apiUrl;

    private static Config config;

    @PostConstruct
    private void init() {
        config = new Config(appId, apiKey, apiUrl);
        log.info("initialize xfyun asr tools complete.");
    }

    /**
     * xf asr
     * see https://doc.xfyun.cn/rest_api/%E8%AF%AD%E9%9F%B3%E5%90%AC%E5%86%99.html
     *
     * @param path 语音文件路径
     */
    public static JSONObject asr(String path) throws Exception {
        //{"engine_type":"sms16k","aue":"raw"} 固定值
        // 业务参数
        String param = "{\"aue\":\"raw\",\"engine_type\":\"sms16k\"}";
        String xParam = new String(Base64.encodeBase64(param.getBytes("UTF-8")));
        // 系统当前秒数时间戳
        String xCurTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
        // 生成令牌
        String xCheckSum = DigestUtils.md5Hex(config.apiKey + xCurTime + xParam);

        // 内容base64
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String audioBase64 = URLEncoder.encode("audio=" + new String(Base64.encodeBase64(bytes), "UTF-8"), "UTF-8");

        String result = postForResult(ImmutableMap.of(
                "Content-Type", "application/x-www-form-urlencoded; charset=utf-8",
                "X-CurTime", xCurTime,
                "X-Param", xParam,
                "X-Appid", config.appId,
                "X-CheckSum", xCheckSum
        ), audioBase64);

        return new JSONObject(result);
    }

    private static String postForResult(Map<String, String> header, String body) {
        String result = "";
        try {
            URL httpUrl = new URL(config.apiUrl);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");

            // 设置 header
            for (Map.Entry<String, String> entry : header.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                conn.setRequestProperty(key, value);
            }

            // 设置请求 body
            conn.setDoOutput(true);
            conn.setDoInput(true);

            //设置连接超时和读取超时时间
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            conn.connect();

            try (PrintWriter out = new PrintWriter(conn.getOutputStream());
                 InputStream is = conn.getInputStream()) {
                out.print(body);
                out.flush();
                result = IOUtils.toString(is, "utf-8");
            } finally {
                conn.disconnect();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @AllArgsConstructor
    private static class Config {
        private String appId;
        private String apiKey;
        private String apiUrl;
    }
}
