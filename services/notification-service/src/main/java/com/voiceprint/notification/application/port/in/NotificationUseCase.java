package com.voiceprint.notification.application.port.in;

import com.voiceprint.notification.domain.Notification;
import com.voiceprint.notification.dto.NotificationEvent;

import java.time.LocalTime;
import java.util.List;

public interface NotificationUseCase {

    void handleNotificationEvent(NotificationEvent event);

//    NotificationListWithCursorDTO getUnreadNotifications(Integer userId, Long cursor, Integer size);

    void markNotification(Integer userId, Long notificationId);

    void publishAllNotifications(List<Notification> notifications);

    // 유저 정보 등록 및 수정
    void handleUserRegisteredEvent(Integer userId, String nickname, String email);
    void handleUserProfileUpdatedEvent(Integer userId, String nickname, String email);
    void handleUserNotificationPreferencesUpdatedEvent(Integer userId, Boolean enableAlarms, LocalTime alarmTime);
}
