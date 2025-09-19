package com.voiceprint.backend.global.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        System.out.printf("Produce message : %s to topic: %s%n", message,topic);
        this.kafkaTemplate.send(topic, message);
    }
}
