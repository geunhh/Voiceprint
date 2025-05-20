package com.voiceprint.backend.api.groups.dto;

import com.voiceprint.backend.domain.Entity.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupUpdateResponse {
    private Integer groupId;
    private String name;
    private String description;
    private String groupImage;
    private Boolean isDeleted;
    private Boolean enableAlarm;
    private List<DayOfWeek> alarmDays;
    private LocalTime alarmTime;

    public static GroupUpdateResponse from(Group group) {
        return new GroupUpdateResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getGroupImage(),
                group.getIsDeleted(),
                group.getEnableAlarm(),
                group.getAlarmDays(),
                group.getAlarmTime()
        );
    }
}
