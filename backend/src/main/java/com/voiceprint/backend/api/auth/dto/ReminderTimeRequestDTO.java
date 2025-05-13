package com.voiceprint.backend.api.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReminderTimeRequestDTO {
    private String alarmTime;
}
