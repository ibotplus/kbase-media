package com.eastrobot.kbs.media.util.ali;

import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizer;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerListener;
import com.alibaba.nls.client.protocol.asr.SpeechRecognizerResponse;
import com.eastrobot.kbs.media.model.Constants;
import com.eastrobot.kbs.media.util.concurrent.ExecutorType;
import com.eastrobot.kbs.media.util.concurrent.ThreadPoolUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ali speech util
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2019-06-11 9:52
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "audio.asr.default", havingValue = Constants.ALI)
public class AliSpeechUtil {
    private static NlsClient client;

    @Value("${convert.audio.asr.ali.apiUrl}")
    private String apiUrl;

    private static String appKey;

    @Value("${convert.audio.asr.ali.token}")
    private String token;

    @Value("${convert.audio.asr.ali.appKey}")
    public void setAppKey(String appKey) {
        AliSpeechUtil.appKey = appKey;
    }

    public static String asr(String filePath) throws InterruptedException, ExecutionException, TimeoutException {
        Future<String> future = ThreadPoolUtil.ofExecutor(ExecutorType.GENERIC_IO_INTENSIVE).submit(() -> {
            AtomicReference<String> atomicResult = new AtomicReference<>();
            try (InputStream ins = new FileInputStream(filePath)) {
                @Cleanup
                SpeechRecognizer recognizer = new SpeechRecognizer(client, new SpeechRecognizerListener() {
                    @Override
                    public void onRecognitionResultChanged(SpeechRecognizerResponse response) {
                    }

                    @Override
                    public void onRecognitionCompleted(SpeechRecognizerResponse response) {
                        String result = response.getRecognizedText();
                        log.info("asr task id: {} recognition completed, recognized text is: {}",
                                response.getTaskId(), result);
                        atomicResult.set(result);
                    }

                    @Override
                    public void onStarted(SpeechRecognizerResponse response) {
                        log.info("asr task id {} started.", response.getTaskId());
                    }

                    @Override
                    public void onFail(SpeechRecognizerResponse response) {
                        log.info("asr task id: {} onFail, status: {}, message: {}", response.getTaskId(),
                                response.getStatus(), response.getStatusText());
                    }
                });
                recognizer.setAppKey(appKey);
                recognizer.setFormat(InputFormatEnum.PCM);
                recognizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
                // 加标点
                recognizer.setEnablePunctuation(true);
                // 中文数字转阿拉伯
                recognizer.setEnableITN(true);
                recognizer.start();
                recognizer.send(ins, 3200, 100);
                recognizer.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return atomicResult.get();
        });

        // 嘿,在这等待~
        return future.get(15, TimeUnit.MINUTES);
    }

    @PostConstruct
    private void init() {
        client = new NlsClient(apiUrl, token);
        log.info("initialize ali asr tools complete.");
    }

    @PreDestroy
    private void shutdown() {
        client.shutdown();
        log.info("shutdown ali asr tools complete.");
    }
}
