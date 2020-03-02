package com.eastrobot.kbs.media.common.util.asr;

@FunctionalInterface
public interface TranscriberCallBack {
    void callback(TranscriberResponse transcriberResponse);
}
