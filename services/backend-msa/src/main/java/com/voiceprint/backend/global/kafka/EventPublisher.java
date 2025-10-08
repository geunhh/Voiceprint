package com.voiceprint.backend.global.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.global.event.NotificationEvent;
import com.voiceprint.backend.global.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${voiceprint.topics.user-events:user-events}")
    private String userTopic;

    @Value("${voiceprint.topics.send-notification:send-notification}")
    private String notiTopic;

    /** 유저 이벤트 발행 */
    public void publishUserEvent(UserEvent event) {
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getOccurredAt() == null || event.getOccurredAt().isBlank()) {
            event.setOccurredAt(OffsetDateTime.now().toString());
        }
        send(userTopic, String.valueOf(event.getUserId()), event);
    }

    /** 알림 트리거 발행 */
    public void publishNotificationEvent(NotificationEvent event) {

        // 1) eventId 보장
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getOccurredAt() == null || event.getOccurredAt().isBlank()) {
            event.setOccurredAt(OffsetDateTime.now().toString());
        }
        final String key = event.getEventId(); // 키=eventId 권장.

        send(notiTopic, String.valueOf(event.getRecipientId()), event);

    }

    private void send(String topic, String key, Object event) {
        try {
            // 2)JSON 직렬화 (String 으로 전송
            String payload = objectMapper.writeValueAsString(event);

            // 3) 토픽/키로 전송
            kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event to topic {} key={} payload={}", topic, key, payload, ex);
                } else {
                    var meta = result.getRecordMetadata();
                    log.info("Published notification event. topic={} partition={} offset={} key={}",
                            meta.topic(), meta.partition(), meta.offset(), key);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
