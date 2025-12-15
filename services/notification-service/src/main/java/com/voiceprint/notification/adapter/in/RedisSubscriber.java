package com.voiceprint.notification.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.adapter.out.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SseEmitterManager emitterManager;
    private final ObjectMapper objectMapper;

    @Qualifier("sseSendExecutor")
    private final ThreadPoolTaskExecutor sseExecutor;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);        // 메시지 본문 추출

        try {
            NotificationDTO dto = objectMapper.readValue(json, NotificationDTO.class);  // JSON -> DTO
//            log.debug("notification dto: {}", dto.getMetadata());

            // 그냥 메타에서 userId 꺼내 쓰기
            Object userIdRaw = dto.getMetadata().get("userId");
            if (userIdRaw == null) {
                log.error("[RedisSubscriber] userId 메타데이터 없음: {}", dto.getMetadata());
                return;
            }

            Integer userId = (userIdRaw instanceof Number num)
                    ? num.intValue()
                    : Integer.valueOf(userIdRaw.toString());
            if (emitterManager.hasEmitter(userId)) {
                sseExecutor.execute(() -> {
                    try {
                        emitterManager.sendTo(userId, dto.getType(), dto);
                    } catch (Exception e) {
                        log.debug("[RedisSubscriber] SSE send failed. userId={}, err={}", userId, e.toString());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("[RedisSubscriber] consume failed. payload={}, err={}", json, e.toString());
        }

    }
}