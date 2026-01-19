package com.voiceprint.notification.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RedisPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // @Primary로 지정된 StringRedisTemplate Bean이 주입됨 (단일 Redis: redis:6379)
    public RedisPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;

        // 연결된 Redis 호스트 정보 로깅
        try {
            var connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory instanceof LettuceConnectionFactory lcf) {
                log.info("[RedisPublisher] 연결된 Redis 호스트: {}:{}",
                        lcf.getHostName(), lcf.getPort());
            }
        } catch (Exception e) {
            log.warn("[RedisPublisher] Redis 호스트 정보 조회 실패", e);
        }
    }

    // 단건 버전 publisher
    public void publishNotification(NotificationDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.convertAndSend("notification-channel", json);
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