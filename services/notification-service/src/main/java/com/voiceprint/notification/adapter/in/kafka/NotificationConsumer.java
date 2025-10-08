package com.voiceprint.notification.adapter.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.notification.adapter.out.persistence.ProcessedEventJPARepository;
import com.voiceprint.notification.application.port.in.NotificationEventHandlerPort;
import java.time.LocalTime;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationEventHandlerPort notificationEventHandlerPort;
    private final ObjectMapper objectMapper;
    private final ProcessedEventJPARepository processedEventRepository;

    // 그룹원 추가, 그룹일기 작성, 댓글 작성 알람 등...
    @KafkaListener(
            topics = "send-notification",
            groupId = "voiceprint-notification",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) throws JsonProcessingException {
        NotificationEvent e = objectMapper.readValue(record.value(), NotificationEvent.class);


        String eventId = (e.getEventId() != null && !e.getEventId().isBlank())
                ? e.getEventId() : record.key();
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("missing eventId");

        // 멱등 체크(성공 후 저장 버전)
        if (processedEventRepository.existsById(eventId)) {
            log.info("이미 등록된 eventId : {}",eventId);
            return;
        }


        // 비즈니스 처리
        notificationEventHandlerPort.handleNotificationEvent(e); // 지금은 서비스에 다 때려넣어도 OK


    }

    // 유저 정보 변경 시..
    // 이건 DB 분리 후에 정리합시다요로롱.
    @KafkaListener(topics = "user-events", groupId = "voiceprint-notification")
    public void consumeUserEvents(String message) {
        log.info("Consumed user event message: {}", message);
        try {
            Map<String, Object> eventMap = objectMapper.readValue(message, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            String eventType = (String) eventMap.get("eventType");
            Integer userId = (Integer) eventMap.get("userId");

            if (userId == null) {
                log.error("User event message missing userId: {}", message);
                return;
            }

            switch (eventType) {
                // 유저 등록
                case "USER_REGISTERED":
                    String nickname = (String) eventMap.get("nickname");
                    String email = (String) eventMap.get("email");
                    notificationEventHandlerPort.handleUserRegisteredEvent(userId, nickname, email);
                    break;
                // 유저 프로필 업데이트
                case "USER_PROFILE_UPDATED":
                    String updatedNickname = (String) eventMap.get("nickname");
                    String updatedEmail = (String) eventMap.get("email");
                    notificationEventHandlerPort.handleUserProfileUpdatedEvent(userId, updatedNickname, updatedEmail);
                    break;
                // 유저 알람 정보 갱신
                case "USER_NOTIFICATION_PREFERENCES_UPDATED":
                    Boolean enableAlarms = (Boolean) eventMap.get("enableAlarms");
                    String alarmTimeString = (String) eventMap.get("alarmTime");
                    LocalTime alarmTime = null;
                    if (alarmTimeString != null) {
                        alarmTime = LocalTime.parse(alarmTimeString);
                    }
                    notificationEventHandlerPort.handleUserNotificationPreferencesUpdatedEvent(userId, enableAlarms, alarmTime);
                    break;
                default:
                    log.warn("Unknown user event type: {}", eventType);
                    break;
            }
            log.info("Successfully processed user event: {}", eventType);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user event message: {}", message, e);
        } catch (Exception e) {
            log.error("Failed to process user event: {}", message, e);
        }
    }
}
