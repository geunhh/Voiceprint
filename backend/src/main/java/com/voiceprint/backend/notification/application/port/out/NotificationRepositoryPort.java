package com.voiceprint.backend.notification.application.port.out;

import com.voiceprint.backend.notification.domain.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findMyNotifications(Integer userId, Long cursor, int limit);
    void markAsRead(Long notificationId, Integer userId);
    List<Notification> saveAll(List<Notification> notifications);
}