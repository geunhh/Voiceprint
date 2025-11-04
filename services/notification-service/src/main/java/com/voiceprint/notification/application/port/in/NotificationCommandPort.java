package com.voiceprint.notification.application.port.in;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceJpaEntity;
import com.voiceprint.notification.domain.Notification;

import java.util.List;

public interface NotificationCommandPort {
    void sendAndSave(UserNotificationPreferenceJpaEntity user, NotificationDTO dto);

    void sendAndSaveWithNewTransaction(UserNotificationPreferenceJpaEntity user, NotificationDTO dto);

    void markNotification(Integer userId, Long notificationId);

    void publishAllNotifications(List<Notification> notifications);
}