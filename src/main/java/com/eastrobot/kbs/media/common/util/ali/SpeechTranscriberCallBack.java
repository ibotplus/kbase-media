package com.eastrobot.kbs.media.common.util.ali;

import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;

@FunctionalInterface
public interface SpeechTranscriberCallBack {
    void callback(SpeechTranscriberResponseType type, SpeechTranscriberResponse resp);
}
