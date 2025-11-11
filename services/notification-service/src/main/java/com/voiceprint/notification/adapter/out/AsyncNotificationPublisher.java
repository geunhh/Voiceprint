package com.voiceprint.notification.adapter.out;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncNotificationPublisher {

    private final RedisPublisher redisPublisher;

    @Async("notificationExecutor")
    public void publishAllAsync(List<NotificationDTO> dtos) {
        long pubStart = System.nanoTime();
        for (NotificationDTO dto : dtos) {
            try {
                redisPublisher.publishNotification(dto);
            } catch (Exception e) {
                log.error("[AsyncNotificationPublisher] publish failed: {}", dto, e);
            }
        }
        long pubEnd = System.nanoTime();
        double elapsedMs = (pubEnd - pubStart) / 1_000_000.0;
        commonLog(elapsedMs);
    }

    @Async("notificationExecutor")
    public void publishAllAsyncWithPipeline(List<NotificationDTO> dtos) {
        long pubStart = System.nanoTime();
        redisPublisher.publishBatchWithPipeline(dtos);
        long pubEnd = System.nanoTime();
        double elapsedMs = (pubEnd - pubStart) / 1_000_000.0;

        commonLog(elapsedMs);
    }

    private static void commonLog(double elapsedMs) {
        log.info("[AsyncPublisher] Done. elapsed={}ms, thread={}",
                String.format("%.2f", elapsedMs), Thread.currentThread().getName());
    }
}