package com.eastrobot.kbs.media.common.util.asr;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.websocket.Session;

/**
 * 转写引擎 init -> (start -> push -> stop) -> destroy 通常init和destroy同服务的生命周期
 */
public interface ITranscriberEngine extends InitializingBean, DisposableBean {

    /**
     * 获取引擎类型
     */
    AsrEngine ofType();

    /**
     * 启动转录器, 每次都要开启一个新的, 并开始接受转写结果
     */
    Transcriber openTranscriber(TranscriberCallBack callBack, Session session);


}
