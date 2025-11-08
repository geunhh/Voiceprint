package com.voiceprint.notification.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.notification.domain.Notification;
import com.voiceprint.notification.adapter.out.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final SseEmitterManager emitterManager;
    private final ObjectMapper objectMapper;
    private final NotificationRepositoryPort notificationRepositoryPort;

    @Override
    @SneakyThrows // Todo : ???? 머임
    public void onMessage(Message message, byte[] pattern) {
        log.debug("message:{}",message);
        String json = new String(message.getBody(), StandardCharsets.UTF_8);        // 메시지 본문 추출
        log.debug("message.body:{}",message.getBody());

        // 파싱 : 앞 뒤 \" \" 로 인해 에러 발생. -> 제거
        if (json.startsWith("\"") && json.endsWith("\"")) {
            json = json.substring(1, json.length() -1);
            json = json.replace("\\\"", "\"");
        }

        NotificationDTO dto = objectMapper.readValue(json, NotificationDTO.class);  // JSON -> DTO
        log.debug("notification dto: {}",dto.getMetadata());

        // 그냥 메타에서 userId 꺼내 쓰기
        Object userIdRaw = dto.getMetadata().get("userId");
        if (userIdRaw == null) {
            log.error("[RedisSubscriber] userId 메타데이터 없음: {}", dto.getMetadata());
            return;
        }

        Integer userId;
        if (userIdRaw instanceof Number num) {
            userId = num.intValue();
        } else {
            userId = Integer.valueOf(userIdRaw.toString());
        }

        log.debug("현재 접속중인 emitter : {} ", emitterManager.getAllEmitterIds());

        if (emitterManager.hasEmitter(userId)) {
            emitterManager.sendTo(userId, dto.getType(), dto); // SSE 전송
            log.info("[RedisSubscriber] 실시간 알림 전송 완료: userId={}, type={}", userId, dto.getType());
        } else {
            log.info("알림을 전달할 구독자가 접속중이 아닙니다. {}", userId);
        }
    }
}