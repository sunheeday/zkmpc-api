package com.zkrypto.zkmpc_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

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
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(512 * 1024); // 512KB (메시지 전체 크기 제한)
        registration.setSendBufferSizeLimit(1024 * 1024); // 1MB (버퍼 사이즈 제한)
        registration.setSendTimeLimit(40 * 1000); // 40초 (전송 시간 제한)
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        container.setMaxTextMessageBufferSize(512 * 1024);
        container.setMaxBinaryMessageBufferSize(512 * 1024);
        container.setMaxSessionIdleTimeout(20 * 1000L);

        return container;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 설정 (클라이언트가 접속할 URL)
        // CORS 문제 해결을 위해 setAllowedOriginPatterns("*") 사용
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}