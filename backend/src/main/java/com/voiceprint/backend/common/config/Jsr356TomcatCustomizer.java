package com.voiceprint.backend.common.config;

import jakarta.websocket.server.ServerContainer;

import org.apache.catalina.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Jsr356TomcatCustomizer {

    private static final Logger log = LoggerFactory.getLogger(Jsr356TomcatCustomizer.class);

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> websocketContainerCustomizer() {
        return factory -> factory.addContextCustomizers((Context context) -> {
            ServerContainer serverContainer = (ServerContainer)
                    context.getServletContext()
                            .getAttribute("jakarta.websocket.server.ServerContainer");
            if (serverContainer != null) {
                serverContainer.setDefaultMaxTextMessageBufferSize(2 * 1024 * 1024);
                serverContainer.setDefaultMaxBinaryMessageBufferSize(2 * 1024 * 1024);
                log.info("🛠 JSR-356 WebSocket 버퍼 설정 적용: text={} / binary={}",
                        serverContainer.getDefaultMaxTextMessageBufferSize(),
                        serverContainer.getDefaultMaxBinaryMessageBufferSize());
            } else {
                log.warn("⚠️ ServerContainer를 찾을 수 없어 버퍼 설정을 스킵했습니다.");
            }
        });
    }
}
