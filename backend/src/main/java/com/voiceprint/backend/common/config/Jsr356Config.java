package com.voiceprint.backend.common.config;

import com.voiceprint.backend.service.chat.voice.AIServerEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class Jsr356Config {
    /**
     * @ServerEndpoint 애노테이션이 붙은 클래스를 JSR-356 컨테이너에 등록해 줍니다.
     * 따로 스캔 애노테이션이 필요 없고, 이 빈만 추가하면 됩니다.
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        ServerEndpointExporter exporter = new ServerEndpointExporter();
        // 명시적으로 등록할 엔드포인트가 하나라면 이렇게도 설정할 수 있습니다.
        exporter.setAnnotatedEndpointClasses(AIServerEndpoint.class);
        return exporter;
    }
}

