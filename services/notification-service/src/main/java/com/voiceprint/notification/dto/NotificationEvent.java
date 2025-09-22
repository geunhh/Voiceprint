package com.voiceprint.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private Integer recipientId; // The ID of the user who should receive the notification
    private String type;
    private String message;
    private Map<String, Object> metadata;
}
