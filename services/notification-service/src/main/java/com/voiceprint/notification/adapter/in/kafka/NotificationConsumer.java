package com.voiceprint.notification.adapter.in.kafka;

import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    // send-notification 토픽에 메시지가 들어오면 이 메소드 실행

//    @KafkaListener(topics = "send-notification", groupId = "voiceprint-notification")
    public void consume(String message) {

        System.out.println("Consumed message :"+message);
        System.out.println(String.format("Consumed message %s:", message));

        // TODO: 실제 푸시 알림 로직 구현.

    }

}
