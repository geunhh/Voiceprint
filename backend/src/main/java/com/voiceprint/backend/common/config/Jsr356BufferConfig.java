package com.voiceprint.backend.common.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.websocket.server.ServerContainer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class Jsr356BufferConfig {

    private static final Logger log = LoggerFactory.getLogger(Jsr356BufferConfig.class);

    @Bean
    public ServletContextInitializer websocketInitializer() {
        return servletContext -> {
            ServerContainer serverContainer =
                    (ServerContainer) servletContext.getAttribute("jakarta.websocket.server.ServerContainer");
            if (serverContainer != null) {
                serverContainer.setDefaultMaxBinaryMessageBufferSize(2 * 1024 * 1024);
                serverContainer.setDefaultMaxTextMessageBufferSize(2 * 1024 * 1024);
                log.info("🛠 JSR-356 WebSocket 버퍼 설정 적용: text={} / binary={}",
                        serverContainer.getDefaultMaxTextMessageBufferSize(),
                        serverContainer.getDefaultMaxBinaryMessageBufferSize());
            } else {
                log.warn("⚠️ ServerContainer를 찾을 수 없어 버퍼 설정을 스킵했습니다.");
            }
        };
    }
}

