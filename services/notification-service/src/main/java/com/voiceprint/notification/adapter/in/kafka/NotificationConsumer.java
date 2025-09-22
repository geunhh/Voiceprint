package com.voiceprint.notification.adapter.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.notification.application.port.in.NotificationUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.voiceprint.notification.dto.NotificationEvent;
import java.time.LocalTime;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationUseCase notificationUseCase;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "send-notification", groupId = "voiceprint-notification")
    public void consume(String message) {

        log.info("Consumed message : {}", message);
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            notificationUseCase.handleNotificationEvent(event);
            log.info("Successfully processed notification evetn for user : {}",event.getRecipientId());
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize notification event : {}",message);
        } catch (Exception e) {
            log.error("Failed to process notification Exception : {}",message, e);
        }


    }

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
                    notificationUseCase.handleUserRegisteredEvent(userId, nickname, email);
                    break;
                // 유저 프로필 업데이트
                case "USER_PROFILE_UPDATED":
                    String updatedNickname = (String) eventMap.get("nickname");
                    String updatedEmail = (String) eventMap.get("email");
                    notificationUseCase.handleUserProfileUpdatedEvent(userId, updatedNickname, updatedEmail);
                    break;
                // 유저 알람 정보 갱신
                case "USER_NOTIFICATION_PREFERENCES_UPDATED":
                    Boolean enableAlarms = (Boolean) eventMap.get("enableAlarms");
                    String alarmTimeString = (String) eventMap.get("alarmTime");
                    LocalTime alarmTime = null;
                    if (alarmTimeString != null) {
                        alarmTime = LocalTime.parse(alarmTimeString);
                    }
                    notificationUseCase.handleUserNotificationPreferencesUpdatedEvent(userId, enableAlarms, alarmTime);
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
