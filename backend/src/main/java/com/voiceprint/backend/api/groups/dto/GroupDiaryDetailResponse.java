package com.voiceprint.backend.api.groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GroupDiaryDetailResponse {
    private Long groupDiaryId;
    private Long groupId;
    private String groupName;
    private Long diaryId;
    private Long userId;
    private String userName;
    private String userImage;
    private LocalDateTime createdAt;
    private String title;
    private String content;
}
