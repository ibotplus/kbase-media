package com.eastrobot.kbs.media.common.util.asr;

import com.google.common.collect.MapMaker;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentMap;

/**
 * 构建实时转写整句缓冲
 */
@Slf4j
public class TranscriberBufferFactory {
    private static ConcurrentMap<Session, StringBuilder> buffers = new MapMaker().makeMap();

    public static StringBuilder getBuffer(Session session) {
        return buffers.get(session);
    }

    public static void registerBuffer(Session session, StringBuilder buffer) {
//        log.info("register buffer : [{}]", "sessionId:" + session.getId() + ", name:" + session.getUserPrincipal().getName());
        log.info("register buffer : [{}]", "sessionId:" + session.getId() + ", name:" + session.getUserPrincipal());
        buffers.put(session, buffer);
    }

    public static void removeBuffer(Session session) {
        log.info("remove buffer : [{}]", "sessionId:" + session.getId() + ", name:" + session.getUserPrincipal());
        buffers.remove(session);
    }
}
