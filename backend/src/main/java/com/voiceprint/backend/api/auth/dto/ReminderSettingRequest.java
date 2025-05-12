package com.voiceprint.backend.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReminderSettingRequest {
    private Boolean enableAlarms;

}
