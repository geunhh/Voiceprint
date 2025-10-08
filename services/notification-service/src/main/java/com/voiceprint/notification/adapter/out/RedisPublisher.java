package com.voiceprint.notification.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishNotification(NotificationDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            log.debug("mapper 후 : {}",json);
            redisTemplate.convertAndSend("notification-channel", json);
            log.debug("[RedisPublisher] 알림 publish 완료 → {}", json);

        } catch (JsonProcessingException e) {
            log.error("[RedisPublisher] 직렬화 실패", e);
        }
    }
}