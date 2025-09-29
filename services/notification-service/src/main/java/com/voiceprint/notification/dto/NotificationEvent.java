package com.voiceprint.notification.dto;

import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private String eventId; // The ID of the user who should receive the notification
    private Integer recipientId;
    private String type;
    private String message;
    private Map<String, Object> metadata;
}
