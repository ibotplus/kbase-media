package com.eastrobot.kbs.media.util.shhan;


import com.alibaba.fastjson.JSONObject;
import com.eastrobot.kbs.media.model.Constants;
import com.eastrobot.kbs.media.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "audio.asr.default", havingValue = Constants.SHHAN)
public class ShhanAsrUtil {

    @Value("${convert.audio.asr.shhan.base-url}")
    private String shhanBaseUrl;

    @Value("${convert.audio.asr.shhan.key}")
    private String key;

    @Value("${convert.audio.asr.shhan.concurrent-number}")
    private String concurrentNumber;

    @Value("${convert.audio.asr.shhan.app-key}")
    private String appKey;

    private static ShhanAsrUtil shhanUtil;

    @PostConstruct
    private void init() {
        shhanUtil = this;
        log.info("initialize shhan asr tools complete.");
    }

    public static String asr(String filePath) {
        byte[] data;
        try {
            data = FileUtils.readFileToByteArray(new File(filePath));
        } catch (IOException e) {
            return "";
        }

        HttpClient client = HttpClientUtil.getHttpClient();
        HttpPost httpPost = new HttpPost(shhanUtil.shhanBaseUrl + "/recog?appKey=" + shhanUtil.appKey);

        httpPost.setHeader("contextCode", "CHN-CMN");
        httpPost.setHeader("sampleRate", "16000");
        httpPost.setHeader("length", String.valueOf(data.length));
        httpPost.setHeader("content-type", "audio/wav");
        // ByteArrayEntity entity = new ByteArrayEntity(data, ContentType.create("audio/basic"));
        ByteArrayEntity entity = new ByteArrayEntity(data, ContentType.create("audio/wav"));
        httpPost.setEntity(entity);
        try {
            HttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.debug("shhan-asr result: [{}] - [{}]", filePath, result);
                return Optional.ofNullable(result)
                        .map(JSONObject::parseObject)
                        .map(v -> v.getString("info"))
                        .orElse("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

}
 