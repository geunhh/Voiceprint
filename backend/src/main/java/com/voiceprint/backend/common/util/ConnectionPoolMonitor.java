package com.voiceprint.backend.common.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionPoolMonitor {

    private final HikariDataSource dataSource;

    // 일반 로그용
    public void logHikariStatus() {
        int active = dataSource.getHikariPoolMXBean().getActiveConnections();
        int idle = dataSource.getHikariPoolMXBean().getIdleConnections();
        int total = dataSource.getHikariPoolMXBean().getTotalConnections();
        int pending = dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection();

        log.info("[HikariCP 상태] Active: {}, Idle: {}, Pending: {}, Total: {}",
                active, idle, pending, total);
    }

    public boolean isConnectionPoolAlmostFull(int threshold) {
        int active = dataSource.getHikariPoolMXBean().getActiveConnections();
        int max = dataSource.getHikariPoolMXBean().getTotalConnections(); // == configured max pool size
        return active >= (max - threshold);
    }
}

