package com.eastrobot.kbs.media.common.util.asr;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.eastrobot.kbs.media.common.util.baidu.BaiduEngine;

import java.io.InputStream;

public class BaiduTranscriber implements Transcriber {
    private RequestMetaData requestMetaData = BaiduEngine.ofMetaData();
    private AsrClient asrClient = BaiduEngine.ofClient();
    private StreamContext streamContext;

    public BaiduTranscriber(StreamContext streamContext) {
        this.streamContext = streamContext;
    }

    @Override
    public void push(InputStream is) {
        try {
            final byte[] data = new byte[asrClient.getFragmentSize(requestMetaData)];

            while (is.read(data) != -1 && !streamContext.getFinishLatch().finished()) {
                streamContext.send(data);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        streamContext.complete();
        try {
            streamContext.getFinishLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
