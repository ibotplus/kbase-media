package com.eastrobot.kbs.media.util.m2;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.kbs.media.model.Constants;
import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.util.HttpClientUtil;
import com.eastrobot.kbs.media.util.ffmpeg.FFmpegUtil;
import com.eastrobot.kbs.media.util.youtu.Base64Util;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2019-04-02 13:43
 */

@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "audio.tts.default", havingValue = Constants.M2)
public class M2TtsUtil {

    @Value("${convert.audio.tts.m2.base-url}")
    private String baseUrl;

    @Value("${convert.sync.output-folder}")
    private String syncOutputFolder;

    private static M2TtsUtil m2TtsUtil;

    @PostConstruct
    private void init() {
        m2TtsUtil = this;
        log.info("initialize m2 tts tools complete.");
    }

    /**
     * 返回音频格式为pcm/16bit/16kHz/1ch
     *
     * @param text 待tts文本内容
     *
     * @return tts 后语音文件base64内容
     */
    public static byte[] tts(String text) {
        HttpClient httpClient = HttpClientUtil.getHttpClient();
        HttpPost httpPost = new HttpPost(m2TtsUtil.baseUrl);
        JSONObject data = new JSONObject()
                .fluentPut("name", "nannan")
                .fluentPut("text", text)
                .fluentPut("speed", 1);
        httpPost.setEntity(new StringEntity(data.toString(), StandardCharsets.UTF_8));
        try {
            HttpResponse response = httpClient.execute(httpPost);
            Optional<HttpEntity> httpEntity = Optional.ofNullable(response)
                    .filter(resp -> HttpStatus.SC_OK == resp.getStatusLine().getStatusCode())
                    .map(HttpResponse::getEntity);
            if (httpEntity.isPresent()) {
                String pcmFile =
                        m2TtsUtil.syncOutputFolder + "/" + System.currentTimeMillis() + FileExtensionType.PCM.pExt();
                String wavFile =
                        m2TtsUtil.syncOutputFolder + "/" + FilenameUtils.getBaseName(pcmFile) + FileExtensionType.WAV.pExt();

                try (InputStream is = httpEntity.get().getContent();
                     FileOutputStream fos = new FileOutputStream(pcmFile)) {
                    IOUtils.copy(is, fos);
                    // ffmpeg
                    FFmpegUtil.ffmepegRun(Lists.newArrayList("-y",
                            "-f", "s16le",
                            "-ar", "16000",
                            "-ac", "1",
                            "-i", pcmFile, wavFile));
                }
                try (FileInputStream in = new FileInputStream(wavFile)) {
                    return IOUtils.readFully(in, in.available());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }
}


