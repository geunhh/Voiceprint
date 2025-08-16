package com.voiceprint.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
    /**
     * CORS 설정을 추가합니다.
     *
     * @param registry CORS 설정을 등록하는 객체
     */

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로에 대해 CORS 설정 적용
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .exposedHeaders("Set-Cookie", "Authorization")
                .allowedOrigins("http://localhost:5173","https://k12b106.p.ssafy.io", 
                                "http://localhost:63342","http://localhost:81"); // TOdo: 개발 후에 수정하기
    }
}
