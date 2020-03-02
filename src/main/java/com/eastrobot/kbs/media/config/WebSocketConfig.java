package com.eastrobot.kbs.media.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


/**
 * @see  <a hrep="https://blog.csdn.net/haoyuyang/article/details/53364372"></a>
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 设置服务器广播消息的基础路径
        registry.enableSimpleBroker("/topic");
        // 设置客户端订阅消息的基础路径
        registry.setApplicationDestinationPrefixes("/app");
        //
        registry.setUserDestinationPrefix("/user");

        // 这句话表示在topic和user这两个域上可以向客户端发消息。
        // registry.enableSimpleBroker("/topic", "/user");
        // 这句话表示客户单向服务器端发送时的主题上面需要加"/app"作为前缀。
        // registry.setApplicationDestinationPrefixes("/app");
        // 这句话表示给指定用户发送一对一的主题前缀是"/user"。
        // registry.setUserDestinationPrefix("/user");


        //   Use this for enabling a Full featured broker like RabbitMQ
        /*
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");
        */
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(5 * 1024 * 1024);
    }

    @Bean
    public WebSocketEventListener webSocketEventListener() {
        return new WebSocketEventListener();
    }

    class WebSocketEventListener {

        @Autowired
        private SimpMessageSendingOperations messagingTemplate;

        @EventListener
        public void handleWebSocketConnectListener(SessionConnectedEvent event) {
            log.info("Received a new web socket connection");
            //
            StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
            log.debug(String.format("%s", sha.getSessionId()));
            //login get from browser
//            UsernamePasswordAuthenticationToken simpUser = (UsernamePasswordAuthenticationToken) sha.getHeader(
//                    "simpUser");
//            String simpSessionId = (String) sha.getHeader("simpSessionId");
//            if (simpUser != null && simpUser.getPrincipal() != null) {
//                String agentId = simpUser.getPrincipal().toString();
//                String sessionId = sha.getSessionId();
//                log.debug(String.format("%s --> %s", agentId, sessionId));
//            }
        }

        @EventListener
        public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            // 用户关闭连接（广播通知）
            String username = (String) headerAccessor.getSessionAttributes().get("username");
            if (username != null) {
                log.info("User Disconnected : " + username);

                messagingTemplate.convertAndSend("/topic/public", "close");
            }
        }
    }
}
