package com.voiceprint.backend.user.application.port.in;

import com.voiceprint.backend.user.adapter.in.web.dto.AlarmSettingsResponseDTO;

public interface ManageAlarmSettingsUseCase {
    AlarmSettingsResponseDTO isReminderEnabled(Integer userId);
    Boolean updateReminderSetting(Boolean enableAlarms, Integer userId);
    String updateReminderTime(String alarmTime, Integer userId);
}
