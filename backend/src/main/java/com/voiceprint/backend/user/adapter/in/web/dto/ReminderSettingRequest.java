package com.voiceprint.backend.user.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReminderSettingRequest {
    private Boolean enableAlarms;

}
