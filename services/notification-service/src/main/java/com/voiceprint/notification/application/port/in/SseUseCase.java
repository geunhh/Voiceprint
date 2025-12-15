package com.voiceprint.notification.application.port.in;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

public interface SseUseCase {
    SseEmitter subscribe(Integer userId);
    void sendNotification(Integer userId, String eventName, NotificationDTO payload);
    void redisPublishFloodAsync(int count);

    Set<Integer> getSubscribedUserIds();

    void broadcast(String eventName, Object payload);
}
