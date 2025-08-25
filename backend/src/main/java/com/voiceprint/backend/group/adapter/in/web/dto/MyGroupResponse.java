package com.voiceprint.backend.group.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyGroupResponse {
    private Integer groupId;
    private String groupName;
    private String groupImageUrl;
    private Integer memberCount;
    private List<String> memberProfileImages; // 최대 3명
    private LocalDateTime createdAt;
}
