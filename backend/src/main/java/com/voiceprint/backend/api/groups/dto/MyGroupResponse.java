package com.voiceprint.backend.api.groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyGroupResponse {
    private Long groupId;
    private String groupName;
    private String groupImageUrl;
    private int memberCount;
    private List<String> memberProfileImages; // 최대 3명
    private LocalDateTime createdAt;
}
