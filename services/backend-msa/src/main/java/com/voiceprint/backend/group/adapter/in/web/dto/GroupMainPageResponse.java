package com.voiceprint.backend.group.adapter.in.web.dto;

import com.voiceprint.backend.group.domain.Group;
import com.voiceprint.backend.group.domain.GroupUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupMainPageResponse {
    private Integer groupId;
    private String name;
    private String description;
    private Boolean enableAlarm;
    private List<DayOfWeek> alarmDays;
    private LocalTime alarmTime;
    private LocalDateTime createdAt;
    private List<UserInfoDTO> groupUserList;
    private LocalDateTime joinedAt;
    private String groupImage;
    private GroupUser.Role role;

    public static GroupMainPageResponse from(Group group, GroupUser currentUserGroupUser, List<UserInfoDTO> groupUserList) {
        return new GroupMainPageResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getEnableAlarm(),
                group.getAlarmDays(),
                group.getAlarmTime(),
                group.getCreatedAt(),
                groupUserList,
                currentUserGroupUser.getJoinedAt(),
                group.getGroupImage(),
                currentUserGroupUser.getRole()
        );
    }
}