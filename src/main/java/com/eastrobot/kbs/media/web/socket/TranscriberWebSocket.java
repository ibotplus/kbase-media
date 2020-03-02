package com.eastrobot.kbs.media.web.socket;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.kbs.media.common.util.AppContext;
import com.eastrobot.kbs.media.common.util.asr.Transcriber;
import com.eastrobot.kbs.media.common.util.asr.TranscriberBufferFactory;
import com.eastrobot.kbs.media.common.util.asr.TranscriberEngineFactory;
import com.eastrobot.kbs.media.config.ConvertAsrProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Controller
@ServerEndpoint("/native-ws/{uid}")
public class TranscriberWebSocket {
    private Session sessionHolder;
    private Transcriber transcriber;

    @OnOpen
    public void onOpen(Session session) {
        session.setMaxIdleTimeout(0);
        session.setMaxBinaryMessageBufferSize(10 * 1024 * 1024);
        sessionHolder = session;

        log.info("ws: session: [{}], connected success: [{}]", session.getId(), session.getUserPrincipal());
    }

    @SneakyThrows
    @OnMessage
    public void actionMsg(String action) {
        switch (action) {
            case "start":
                log.info("init transcriber env");
                transcriber =
                        TranscriberEngineFactory.of(AppContext.ofCtx().getBean(ConvertAsrProperties.class).getAsrEngine())
                        .openTranscriber(resp -> {
                            try {
                                sessionHolder.getBasicRemote().sendText(
                                        new JSONObject()
                                                .fluentPut("msg", resp.getMsg())
                                                .fluentPut("speed", resp.getSpeed())
                                                .toString()
                                );
                            } catch (IOException e) {
                                log.error("send message to client occurred exception");
                                e.printStackTrace();
                            }
                        }, sessionHolder);
                break;
            case "stop":
                log.info("clean transcriber env");
                transcriber.close();
                TranscriberBufferFactory.removeBuffer(sessionHolder);
                break;
            default:
                break;
        }
    }

    @OnMessage
    public void onMessage(InputStream is) throws Exception {
        log.info("ws: receive data length: {}", is.available());
        transcriber.push(is);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        log.warn("ws: session: [{}], connection close: [{}]", session.getId(), session.getUserPrincipal());
        session.close();
        TranscriberBufferFactory.removeBuffer(sessionHolder);
        transcriber.close();
    }

    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.debug("ws: session: [{}], connection error: [{}]", session.getId(), session.getUserPrincipal());
        error.printStackTrace();
    }
}
