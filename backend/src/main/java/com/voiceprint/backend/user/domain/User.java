package com.voiceprint.backend.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;

    private Byte profileImageId;

    private String providerId;

    private String nickname;

    private AuthProvider authProvider;

    private boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer usingThemaId;

    private Byte lastChatbotId;

    private Boolean enableAlarm;

    private LocalTime alarmTime;

    public User withNickname(String newNickname) {
        return new User(this.id, this.profileImageId, this.providerId, newNickname, this.authProvider, this.isDeleted, this.createdAt,
                        this.updatedAt, this.usingThemaId, this.lastChatbotId, this.enableAlarm, this.alarmTime);
    }

    public User withProfileImageId(Byte newProfileImageId) {
        return new User(this.id, newProfileImageId, this.providerId, this.nickname, this.authProvider, this.isDeleted, this.createdAt, this.updatedAt, this.usingThemaId, this.lastChatbotId, this.enableAlarm, this.alarmTime);
    }

    public User withEnableAlarm(Boolean enableAlarm) {
        return new User(this.id, this.profileImageId, this.providerId, this.nickname, this.authProvider, this.isDeleted, this.createdAt, this.updatedAt, this.usingThemaId, this.lastChatbotId, enableAlarm, this.alarmTime);
    }

    public User withAlarmTime(LocalTime alarmTime) {
        return new User(this.id, this.profileImageId, this.providerId, this.nickname, this.authProvider, this.isDeleted, this.createdAt, this.updatedAt, this.usingThemaId, this.lastChatbotId, this.enableAlarm, alarmTime);
    }
}
