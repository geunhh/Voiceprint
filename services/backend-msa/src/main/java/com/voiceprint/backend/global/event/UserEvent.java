package com.voiceprint.backend.global.event;

import lombok.Data;

// core & consumers 공유 DTO (가벼운 공통 모듈 추천)
@Data
public class UserEvent {
    private String eventId;       // UUID 문자열
    private String eventType;     // USER_REGISTERED | USER_PROFILE_UPDATED | USER_NOTIFICATION_PREFERENCES_UPDATED
    private Integer userId;
    private Boolean enableAlarms; // 선택
    private String alarmTime;     // "HH:mm" 선택
    private String occurredAt;    // ISO-8601
    private Integer eventVersion = 1;
}