package com.voiceprint.backend.api.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationDTO {
    private String type;         // 예: "comment", "reminder"
    private String message;      // 알림 메시지
    private String targetType;   // ID 종류
    private Long targetId;       // 예: diaryId 등
}
