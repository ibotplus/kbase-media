package com.eastrobot.kbs.media.common.util.asr;

import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.eastrobot.kbs.media.common.util.ali.AliEngine;
import com.eastrobot.kbs.media.common.util.ali.SpeechTranscriberResponseType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.Optional;

@Slf4j
@Component
class AliTranscriberEngine implements ITranscriberEngine {

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
        return AsrEngine.ALI_NLS;
    }

    @Override
    public Transcriber openTranscriber(TranscriberCallBack callBack, Session session) {
        SpeechTranscriber transcriber = AliEngine.speechTranscribe((type, resp) -> {
            if (SpeechTranscriberResponseType.onTranscriptionResultChange.equals(type)
                    || SpeechTranscriberResponseType.onSentenceEnd.equals(type)) {
                String transSentenceText = resp.getTransSentenceText();

                if (StringUtils.isNotBlank(transSentenceText)) {
                    sentenceBuffer = TranscriberBufferFactory.getBuffer(session);
                    String sendText;
                    if (type.equals(SpeechTranscriberResponseType.onSentenceEnd)) {
                        sentenceBuffer.append(transSentenceText);
                        sendText = sentenceBuffer.toString();
                    } else {
                        sendText = sentenceBuffer.toString() + transSentenceText;
                    }

                    String finalSendText = sendText;
                    Integer speed = Optional.ofNullable(resp.getTransSentenceTime())
                            .map(time -> (int) (60000.0 / time * finalSendText.length()))
                            .orElse(0);
                    callBack.callback(TranscriberResponse.builder().msg(finalSendText).speed(speed).build());
                }
            }
        });
        try {
            transcriber.start();
        } catch (Exception e) {
            log.error("ali transcriber engine start failed.");
            e.printStackTrace();
        }
        sentenceBuffer = new StringBuilder(1024);
        TranscriberBufferFactory.registerBuffer(session, sentenceBuffer);

        return new AliTranscriber(transcriber);
    }

    @Override
    public void destroy() {
        AliEngine.ofClient().shutdown();
    }
}
