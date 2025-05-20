package com.voiceprint.backend.api.alarm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
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

    /**
     * NotificationDTO 객체를 JSON으로 직렬화하여 Redis 채널에 publish
     */
    public void publishNotification(NotificationDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);         // JSON 문자열로 변환
            log.debug("mapper 후 : {}",json);
            redisTemplate.convertAndSend("notification-channel", json); // Redis 채널로 publish
            log.debug("[RedisPublisher] 알림 publish 완료 → {}", json);

        } catch (JsonProcessingException e) {
            log.error("[RedisPublisher] 직렬화 실패", e);
        }
    }

}
