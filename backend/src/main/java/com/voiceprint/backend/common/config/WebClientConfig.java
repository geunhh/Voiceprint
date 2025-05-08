package com.voiceprint.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    //FastAPI 서버의 기본 URL 설정
    @Bean
    public WebClient fastApiWebClient() {
        return WebClient.builder()
                .baseUrl(fastApiUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
