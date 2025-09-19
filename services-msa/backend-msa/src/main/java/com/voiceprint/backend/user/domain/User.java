package com.voiceprint.backend.user.domain;

import com.voiceprint.backend.diary.domain.DiaryThema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder(toBuilder = true) // 기존 객체에서 수정사항만 반영해서 Builder 자동 생성
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

    private DiaryThema usingThema;

    private DiaryThema customThema;

    private Byte lastChatbotId;

    private Boolean enableAlarm;

    private LocalTime alarmTime;

    public User withNickname(String newNickname) {
        return this.toBuilder().nickname(newNickname).build();
    }

    public User withProfileImageId(Byte newProfileImageId) {
        return this.toBuilder().profileImageId(newProfileImageId).build();
    }

    public User withEnableAlarm(Boolean enableAlarm) {
        return this.toBuilder().enableAlarm(enableAlarm).build();
    }

    public User withAlarmTime(LocalTime alarmTime) {
        return this.toBuilder().alarmTime(alarmTime).build();
    }

    public User withUsingThema(DiaryThema usingThema) {
        return this.toBuilder().usingThema(usingThema).build();
    }

    public User withCustomThema(DiaryThema customThema) {
        return this.toBuilder().customThema(customThema).build();
    }
}
