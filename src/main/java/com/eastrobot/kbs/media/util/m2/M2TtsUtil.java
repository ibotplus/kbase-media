package com.eastrobot.kbs.media.util.m2;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.util.HttpClientUtil;
import com.eastrobot.kbs.media.util.ffmpeg.FFmpegUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2019-04-02 13:43
 */

@Slf4j
@Component
// @ConditionalOnProperty(prefix = "convert", name = "audio.asr.default", havingValue = Constants.M2)
public class M2TtsUtil {

    @Value("${convert.audio.asr.m2.base-url}")
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
     * @param text 待tts文本内容
     * @return tts的文件
     */
    public static String tts(String text) throws IOException {
        HttpClient httpClient = HttpClientUtil.getHttpClient();
        HttpPost httpPost = new HttpPost(m2TtsUtil.baseUrl);
        // HttpPost httpPost = new HttpPost("http://222.73.111.245:9090");
        JSONObject data = new JSONObject();
        data.put("name", "nannan");
        data.put("text", text);
        data.put("speed", 1);
        httpPost.setEntity(new StringEntity(data.toString(), StandardCharsets.UTF_8));
        String pcmFile = m2TtsUtil.syncOutputFolder + "/" + System.currentTimeMillis() + FileExtensionType.PCM.pExt();
        String wavFile =
                m2TtsUtil.syncOutputFolder + "/" + FilenameUtils.getBaseName(pcmFile) + FileExtensionType.WAV.pExt();

        HttpResponse response = null;
        Optional
                .ofNullable(httpClient.execute(httpPost))
                .filter(resp -> HttpStatus.SC_OK == resp.getStatusLine().getStatusCode())
                .map(HttpResponse::getEntity)
                .ifPresent(e -> {
                    try (InputStream is = e.getContent();
                         FileOutputStream fos = new FileOutputStream(pcmFile)) {
                        IOUtils.copy(is, fos);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

        // ffmpeg -f s16le -ar 16000 -ac 1 -i test.pcm test.wav
        // ffmpeg  -i test.pcm -f s16le -ar 16000 -ac 1 test.wav
        FFmpegUtil
                .ffmepegRun(Lists.newArrayList("-y",
                        "-f", "s16le",
                        "-ar", "16000",
                        "-ac", "1",
                        "-i", pcmFile, wavFile));

        return wavFile;
    }
}


