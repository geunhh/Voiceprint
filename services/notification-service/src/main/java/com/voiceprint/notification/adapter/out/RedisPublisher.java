package com.voiceprint.notification.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {

    @Qualifier("pubsubRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 단건 버전 publisher
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

    // 배치 + 파이프라인 버전
    public void publishBatchWithPipeline(List<NotificationDTO> dtos) {

        long start = System.nanoTime();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var stringSerializer = redisTemplate.getStringSerializer();
            byte[] channelBytes = stringSerializer.serialize("notification-channel");

            for (NotificationDTO dto : dtos) {
                try {
                    String json = objectMapper.writeValueAsString(dto);
                    byte[] messageBytes = stringSerializer.serialize(json);
                    connection.publish(channelBytes, messageBytes);
                } catch (JsonProcessingException e) {
                    log.warn("[RedisPublisher] serialize failed: {}", dto, e);
                }
            }
            return null;
        });

        long end = System.nanoTime();
        double elapsedMs = (end - start) / 1_000_000.0;
        log.info("[RedisPublisher] pipeline publish {} rows took {}ms", dtos.size(), elapsedMs);
    }

}