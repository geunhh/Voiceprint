package com.voiceprint.backend.group.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GroupDiaryDetailResponse {
    private Integer groupDiaryId;
    private Integer groupId;
    private String groupName;
    private Integer diaryId;
    private Integer userId;
    private String userName;
    private String userImage;
    private LocalDateTime createdAt;
    private String title;
    private String content;
}
