package com.voiceprint.backend.api.alarm.dto;

import com.voiceprint.backend.api.diary.dto.DiarySummaryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationListWithCursorDTO {
    private List<NotificationDTO> diaries;
    private Long nextCursor;
}
