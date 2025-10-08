package com.voiceprint.notification.application.port.in;

import com.voiceprint.notification.adapter.in.kafka.NotificationEvent;

import java.time.LocalTime;

public interface NotificationEventHandlerPort {
    void handleNotificationEvent(NotificationEvent event);

    // 유저 정보 등록 및 수정
    void handleUserRegisteredEvent(Integer userId, String nickname, String email);
    void handleUserProfileUpdatedEvent(Integer userId, String nickname, String email);
    void handleUserNotificationPreferencesUpdatedEvent(Integer userId, Boolean enableAlarms, LocalTime alarmTime);
}