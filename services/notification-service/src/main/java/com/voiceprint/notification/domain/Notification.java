package com.voiceprint.notification.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Notification {

    private final Long id;
    private final Integer userId;
    private final String type;
    private final String message;
    private final Map<String, Object> metadata;

    @Builder.Default
    private final boolean isRead = false;

    @Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();

    public static Notification create(
            Integer userId,
            String type,
            String message,
            Map<String, Object> metadata
    ) {
        return Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .metadata(metadata)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Notification markAsRead() {
        return this.toBuilder()
                .isRead(true)
                .build();
    }
}