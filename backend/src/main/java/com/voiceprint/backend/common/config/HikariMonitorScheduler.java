package com.voiceprint.backend.common.config;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
@RequiredArgsConstructor
public class HikariMonitorScheduler {

    private final DataSource dataSource;
    private HikariDataSource hikariDataSource;

    @PostConstruct
    public void init() {
        if (dataSource instanceof HikariDataSource) {
            this.hikariDataSource = (HikariDataSource) dataSource;
        } else {
            log.warn("⚠️ DataSource is not HikariCP");
        }
    }

    // 매 3초마다 실행
    @Scheduled(fixedDelay = 3000)
    public void logHikariStatus() {
        if (hikariDataSource == null) return;

        log.info("🔍 HikariCP 상태 체크");
        log.info(" - 현재 활성 커넥션 수: {}", hikariDataSource.getHikariPoolMXBean().getActiveConnections());
        log.info(" - 대기 중인 스레드 수: {}", hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        log.info(" - 사용 가능한 커넥션 수: {}", hikariDataSource.getHikariPoolMXBean().getIdleConnections());
        log.info(" - 최대 커넥션 수: {}", hikariDataSource.getMaximumPoolSize());
    }
}