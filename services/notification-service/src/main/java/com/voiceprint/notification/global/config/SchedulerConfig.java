package com.voiceprint.notification.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * SchedulerConfig
 *
 * 스프링의 기본 스케줄러 설정을 커스터마이징하기 위한 구성 클래스.
 * 기본적으로 Spring Scheduler는 단일 스레드에서 모든 @Scheduled 작업을 순차적으로 실행한다.
 *
 * 아래 설정을 통해 ThreadPoolTaskScheduler를 직접 구성하여,
 * 여러 @Scheduled 작업이 동시에 실행될 수 있도록 스레드풀 기반으로 확장한다.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);                       // 동시에 3개 작업 처리
        scheduler.setThreadNamePrefix("scheduler-");    // 로그 추적용 접두사
        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
