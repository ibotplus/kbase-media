package com.eastrobot.kbs.media.common.util.asr;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.eastrobot.kbs.media.common.util.baidu.BaiduEngine;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

@Slf4j
@Component
class BaiduTranscriberEngine implements ITranscriberEngine {
    /**
     * 转写整句缓冲
     */
    private StringBuilder sentenceBuffer;

    @Override
    public void afterPropertiesSet() {
        TranscriberEngineFactory.register(ofType(), this);
    }

    @Override
    public AsrEngine ofType() {
        return AsrEngine.BAIDU_BCE;
    }

    @Override
    public Transcriber openTranscriber(TranscriberCallBack callBack, Session session) {
        // fixme 百度是否也会出现多用户转写问题
        sentenceBuffer = new StringBuilder(1024);
        AsrClient asrClient = BaiduEngine.ofClient();
        RequestMetaData requestMetaData = BaiduEngine.ofMetaData();

        StreamContext streamContext = asrClient.asyncRecognize(it -> {
            log.debug("receive fragment: " + it);
            String sendText = it.getResult();
            if (it.isCompleted()) {
                sentenceBuffer.append(sendText)/*.append(",")*/;
                sendText = sentenceBuffer.toString();
            } else {
                sendText = sentenceBuffer.toString() + sendText;
            }

            // 计算语速
            LocalTime endTime = it.getEndTime();
            int durationTime = (endTime.getSecondOfMinute() * 1000 + endTime.getMillisOfSecond());
            Integer speed = 60000 / durationTime * sendText.length();
            callBack.callback(TranscriberResponse.builder().msg(sendText).speed(speed).build());
        }, requestMetaData);

        return new BaiduTranscriber(streamContext);
    }

    @Override
    public void destroy() {
        BaiduEngine.ofClient().shutdown();
    }
}
