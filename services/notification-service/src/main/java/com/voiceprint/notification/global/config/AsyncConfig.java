package com.voiceprint.notification.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    // Async("notifi...") 형태로 연결
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);        // 기본 스레드
        executor.setMaxPoolSize(8);         // 최대 스레드
        executor.setQueueCapacity(10_000);  // 실행 대기 중인 큐 용량
        executor.setThreadNamePrefix("notification-async-"); // 디버깅 로그
        executor.initialize();
        return executor;
    }
}
