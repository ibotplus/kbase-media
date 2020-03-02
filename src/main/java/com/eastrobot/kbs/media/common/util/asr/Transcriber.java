package com.eastrobot.kbs.media.common.util.asr;

import java.io.Closeable;
import java.io.InputStream;

/**
 * 单流程使用 每次都要通过引擎open一个新的转录器实例
 */
public interface Transcriber extends Closeable {

    /**
     * 推送流去实时转写
     */
    void push(InputStream is);
}
