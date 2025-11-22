package com.voiceprint.backend.user.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.global.event.UserEvent;
import com.voiceprint.backend.user.application.port.out.UserEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventKafkaAdapter implements UserEventPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "user-events";

    @Override
    public void sendUserCreatedEvent(UserEvent event, String key) {
        send(event, key);
    }


    @Override
    public void sendUserUpdatedEvent(UserEvent event, String key) {
        send(event, key);
    }

    @Override
    public void sendUserDeletedEvent(UserEvent event, String key) {
        send(event, key);
    }

    private void send(UserEvent event, String key) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            String traceId = MDC.get("traceId"); // 스케줄러에서 세팅해둔 값

            log.info("Sending message to topic='{}' key={} userId={} traceId={}",
                    TOPIC, key, event.getUserId(), traceId);

            Message<String> message = MessageBuilder
                    .withPayload(jsonMessage)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(KafkaHeaders.KEY, key)
                    .setHeader("X-Trace-Id", traceId) // ★ traceId를 헤더로 전달
                    .build();

            kafkaTemplate.send(message);
        } catch (Exception e) {
            log.error("Failed to send message to kafka topic '{}'", TOPIC, e);
        }
    }
}
