package com.eastrobot.kbs.media.common.util.asr;

import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class AliTranscriber implements Transcriber {
    private SpeechTranscriber speechTranscriber;

    public AliTranscriber(SpeechTranscriber speechTranscriber) {
        this.speechTranscriber = speechTranscriber;
    }

    @Override
    public void push(InputStream is) {
        speechTranscriber.send(is);
    }

    @Override
    public void close() {
        try {
            speechTranscriber.stop();
        } catch (Exception e) {
            log.error("ali transcriber engine stop failed.");
            e.printStackTrace();
        }
        speechTranscriber.close();
    }
}
