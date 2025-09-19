package com.voiceprint.backend.notification.application.port.in;

import com.voiceprint.backend.notification.adapter.in.web.NotificationDTO;
import com.voiceprint.backend.notification.adapter.in.web.NotificationListWithCursorDTO;
import com.voiceprint.backend.notification.domain.Notification;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;

import java.util.List;

public interface NotificationUseCase {
    void sendAndSave(UserJPAEntity user, NotificationDTO dto);
    NotificationListWithCursorDTO getUnreadNotifications(Integer userId, Long cursor, Integer size);
    void markNotification(Integer userId, Long notificationId);
    void publishAllNotifications(List<Notification> notifications);
}
