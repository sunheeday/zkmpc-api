package com.zkrypto.zkmpc_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트에게 메시지를 푸시할 때 사용할 Prefix (Destination) 설정
        // /topic, /queue 접두사가 붙은 메시지는 브로커가 처리
        config.enableSimpleBroker("/topic", "/queue");

        // 애플리케이션에서 처리할 메시지의 Prefix (클라이언트가 서버로 보낼 때 사용)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 설정 (클라이언트가 접속할 URL)
        // CORS 문제 해결을 위해 setAllowedOriginPatterns("*") 사용
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}