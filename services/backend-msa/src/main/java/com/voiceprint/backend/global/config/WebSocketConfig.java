package com.voiceprint.backend.global.config;

import com.voiceprint.backend.chat.adapter.in.web.voice.VoiceChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoiceChatWebSocketHandler voiceChatHandler;
    // backend 병합 후 주석 해제
    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceChatHandler, "/ws")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*"); // 개발환경에서는 모든 오리진 허용, 운영환경에서는 특정 도메인으로 제한
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 더 큰 메시지 크기 허용 (오디오 데이터를 위해)
        container.setMaxTextMessageBufferSize(2 * 1024 * 1024);
        container.setMaxBinaryMessageBufferSize(2 * 1024 * 1024); // 2MB
        // 타임아웃 설정
        container.setMaxSessionIdleTimeout(60000L); // 60초

        return container;
    }
}