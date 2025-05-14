package com.voiceprint.backend.api.groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupMainPageResponse {
    private Long groupId;
    private String name;
    private String description;
    private Boolean enableAlarm;
    private List<DayOfWeek> alarmDays;
    private LocalTime alarmTime;
    private LocalDateTime createdAt;
    private List<UserInfoDTO> groupUserList;
    private LocalDateTime joinedAt;
    private String groupImage;
}
