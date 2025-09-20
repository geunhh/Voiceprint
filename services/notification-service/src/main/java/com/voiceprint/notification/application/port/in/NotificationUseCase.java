package com.voiceprint.notification.application.port.in;

import com.voiceprint.notification.adapter.in.web.NotificationDTO;
import com.voiceprint.notification.adapter.in.web.NotificationListWithCursorDTO;
import com.voiceprint.notification.domain.Notification;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;

import java.util.List;

public interface NotificationUseCase {
    void sendAndSave(UserJPAEntity user, NotificationDTO dto);
    NotificationListWithCursorDTO getUnreadNotifications(Integer userId, Long cursor, Integer size);
    void markNotification(Integer userId, Long notificationId);
    void publishAllNotifications(List<Notification> notifications);
}
