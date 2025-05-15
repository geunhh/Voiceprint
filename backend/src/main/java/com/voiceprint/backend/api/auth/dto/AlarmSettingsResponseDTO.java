package com.voiceprint.backend.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlarmSettingsResponseDTO {
    private String enableAlarms;
    private LocalTime alarmTime;

}
