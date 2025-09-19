package com.voiceprint.backend.notification.application.port.in;

import com.voiceprint.backend.notification.adapter.in.web.NotificationDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

public interface SseUseCase {
    SseEmitter subscribe(Integer userId);
    void sendNotification(Integer userId, String eventName, NotificationDTO payload);
    Set<Integer> getSubscribedUserIds();
}
