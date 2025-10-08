package com.voiceprint.backend.user.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.global.event.UserEvent;
import com.voiceprint.backend.user.application.port.out.UserEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
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
            log.info("Sending message to topic '{}' with key: {}", TOPIC, key);
            kafkaTemplate.send(TOPIC, key, jsonMessage);
        } catch (Exception e) {
            log.error("Failed to send message to kafka topic '{}'", TOPIC, e);
        }
    }
}
