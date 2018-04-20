package com.eastrobot.converter.util.shhan;


import com.eastrobot.converter.model.Constants;
import com.eastrobot.converter.util.HttpClientUtil;
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

@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "audio.asr.default", havingValue = Constants.SHHAN)
public class ShhanAsrUtil {

    @Value("${convert.audio.asr.shhan.base-url}")
    private String shhanBaseUrl;

    private static ShhanAsrUtil shhanUtil;

    @PostConstruct
    public void init() {
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
        HttpPost httpPost = new HttpPost(shhanUtil.shhanBaseUrl + "recogBatch"); // 离线 支持多

        // httpPost.setHeader("requestId", "");
        httpPost.setHeader("contextCode", "CHN-CMN");
        httpPost.setHeader("sampleRate", "16000");
        httpPost.setHeader("length", String.valueOf(data.length));
        ByteArrayEntity entity = new ByteArrayEntity(data, ContentType.create("audio/basic"));
        httpPost.setEntity(entity);
        try {
            HttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
                result = StringUtils.substringAfter(result, "<s>");

                return result.replaceAll("\\s*|\t|\n", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

}
 