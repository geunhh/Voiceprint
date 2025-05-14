package com.voiceprint.backend.api.alarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.domain.Entity.Notification;
import com.voiceprint.backend.domain.Repository.NotificationRepository;
import com.voiceprint.backend.domain.Repository.SseEmitterManager;
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
    private final NotificationRepository notificationRepository;

    @Override
    @SneakyThrows // Todo : ???? 머임
    public void onMessage(Message message, byte[] pattern) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);        // 메시지 본문 추출

        // 파싱 : 앞 뒤 " " 로 인해 에러 발생. -> 제거
        if (json.startsWith("\"") && json.endsWith("\"")) {
            json = json.substring(1, json.length() -1);
            json = json.replace("\\\"","\"");
        }

        NotificationDTO dto = objectMapper.readValue(json, NotificationDTO.class);  // JSON -> DTO

        Long notificationId = Long.valueOf(dto.getMetadata().get("notificationId").toString());
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        log.info("notificiation : {}",notification);
        if (notification == null) {
            log.warn("[RedisSubscriber] 해당 알림 없음: {}", notificationId);
            return;
        }

        Long userId = notification.getUser().getId();

        if (emitterManager.hasEmitter(userId)) {
            emitterManager.sendTo(userId,dto.getType(),dto); // SSE 전송
            log.info("[RedisSubscriber] 실시간 알림 전송 완료: userId={}, type={}", userId, dto.getType());
        }
        else {
            log.info("알림을 전달할 구독자가 접속중이 아닙니다.");
        }
    }
}

/**
 * 참고
 *
 2025-05-14T17:17:00.268+09:00  INFO 8400 --- [voiceprint] [edisContainer-1] c.v.backend.api.alarm.RedisSubscriber    : notificiation : com.voiceprint.backend.domain.Entity.Notification@638aa265
 2025-05-14T17:17:00.268+09:00  INFO 8400 --- [voiceprint] [edisContainer-2] c.v.backend.api.alarm.RedisSubscriber    : notificiation : com.voiceprint.backend.domain.Entity.Notification@72ca7253
 2025-05-14T17:17:00.268+09:00  INFO 8400 --- [voiceprint] [edisContainer-1] c.v.backend.api.alarm.RedisSubscriber    : 알림을 전달할 구독자가 접속중이 아닙니다.
 2025-05-14T17:17:00.268+09:00  INFO 8400 --- [voiceprint] [edisContainer-2] c.v.backend.api.alarm.RedisSubscriber    : 알림을 전달할 구독자가 접속중이 아닙니다.
 * 이런식으로 단일 아림을 보낸 것 같은데, RedisSubscriber.onMessage()가 두 번 동시ㅔ 실행된 것 ㅊ첢 보임.
 * -> Redis Pub/Sub은 내부적으로 멀티스레드로 메시지를 처리함.
 */