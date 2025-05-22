package com.voiceprint.backend.api.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private String type;                    // 예: "comment", "reminder"
    private String message;                 // 알림 메시지
    private Map<String, Object> metadata;   // 메타 데이터
}
