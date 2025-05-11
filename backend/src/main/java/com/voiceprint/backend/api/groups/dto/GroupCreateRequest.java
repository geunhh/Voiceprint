package com.voiceprint.backend.api.groups.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateRequest {
    private String name;
    private String description;
    private String groupImage;
    private Boolean enableAlarm;
    private LocalDateTime alarm;
}

