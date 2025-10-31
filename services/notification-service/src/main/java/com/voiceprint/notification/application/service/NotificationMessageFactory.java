package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;

public interface NotificationMessageFactory {
    NotificationDTO createNotification(String status);
}
