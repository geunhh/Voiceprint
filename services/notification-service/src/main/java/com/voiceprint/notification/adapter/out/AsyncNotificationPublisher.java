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
        for (NotificationDTO dto : dtos) {
            try {
                redisPublisher.publishNotification(dto);
            } catch (Exception e) {
                log.error("[AsyncNotificationPublisher] publish failed: {}", dto, e);
            }
        }
    }
}