package com.voiceprint.notification.application.port.in;

import com.voiceprint.notification.adapter.in.web.dto.NotificationListWithCursorDTO;

public interface NotificationQueryPort {
    NotificationListWithCursorDTO getUnreadNotifications(Integer userId, Long cursor, Integer size);
}