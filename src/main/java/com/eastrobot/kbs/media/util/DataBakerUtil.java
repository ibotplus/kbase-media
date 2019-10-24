package com.eastrobot.kbs.media.util;

import com.eastrobot.kbs.media.model.Constants;
import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.util.ffmpeg.FFmpegUtil;
import com.eastrobot.kbs.media.util.m2.M2TtsUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sun.net.www.protocol.http.HttpCallerInfo;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "audio.tts.default", havingValue = Constants.DATA_BAKER)
public class DataBakerUtil {

    @Value("${convert.audio.tts.baker.base-url}")
    private String baseUrl;

    @Value("${convert.audio.tts.baker.access-token}")
    private String accessToken;

    @Value("${convert.audio.tts.baker.domain}")
    private String domain;

    @Value("${convert.audio.tts.baker.voice-name}")
    private String voiceName;

    @Value("${convert.audio.tts.baker.language}")
    private String language;

    private static DataBakerUtil bakerUtil;

    @PostConstruct
    public void init() {
        bakerUtil = this;
        log.info("initialize data-baker tts tools complete.");
    }

    public static byte[] tts(String text) {
        HttpClient httpClient = HttpClientUtil.getHttpClient();
        HttpGet httpGet = new HttpGet();
        String actionUrl = String.format(bakerUtil.baseUrl +
                        "?access_token=%s&language=%s&domain=%s&voice_name=%s&text=%s",
                bakerUtil.accessToken, bakerUtil.language, bakerUtil.domain, bakerUtil.voiceName, text);
        httpGet.setURI(URI.create(actionUrl));
        try {
            HttpResponse response = httpClient.execute(httpGet);
            Optional<HttpEntity> httpEntity = Optional.ofNullable(response)
                    .filter(resp -> HttpStatus.SC_OK == resp.getStatusLine().getStatusCode())
                    .map(HttpResponse::getEntity);

            if (httpEntity.isPresent()) {
                try (InputStream is = httpEntity.get().getContent()) {
                    return IOUtils.readFully(is, is.available());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }
}
