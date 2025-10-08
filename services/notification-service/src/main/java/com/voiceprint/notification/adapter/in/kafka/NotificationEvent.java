package com.voiceprint.notification.adapter.in.kafka;

import lombok.Data;

import java.util.Map;

@Data
public class NotificationEvent {
    private String eventId;          // UUID
    private Integer recipientId;
    private String eventType;        // ex. "GROUP_MEMBER_ADDED"
    private String message;          // 알림 본문
    private Map<String, Object> metadata;
    private String occurredAt;   // ISO-8601
    private Integer eventVersion = 1;

}