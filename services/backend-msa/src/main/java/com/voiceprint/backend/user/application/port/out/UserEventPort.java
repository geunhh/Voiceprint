package com.voiceprint.backend.user.application.port.out;

import com.voiceprint.backend.global.event.UserEvent;

public interface UserEventPort {
    void sendUserCreatedEvent(UserEvent event, String key);
    void sendUserUpdatedEvent(UserEvent event, String key);
    void sendUserDeletedEvent(UserEvent event, String key);
}
