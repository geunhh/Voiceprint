package com.voiceprint.notification.application.port.out;

import com.voiceprint.notification.domain.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findMyNotificationsV1(Integer userId, Long cursor, int limit);

    List<Notification> findMyNotificationsV2(Integer userId, Long cursor, int limit);

    void markAsRead(Long notificationId, Integer userId);
    List<Notification> saveAll(List<Notification> notifications);
}