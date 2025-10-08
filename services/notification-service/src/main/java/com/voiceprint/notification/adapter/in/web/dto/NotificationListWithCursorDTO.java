package com.voiceprint.notification.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationListWithCursorDTO {
    private List<NotificationDTO> diaries;
    private Long nextCursor;
}