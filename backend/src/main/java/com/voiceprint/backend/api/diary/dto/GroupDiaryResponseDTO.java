package com.voiceprint.backend.api.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupDiaryResponseDTO {
    private Long groupId;
    private Long diaryId;
    private String title;
    private String content;
    private String createdAt;
    private String profileUrl;
    private String nickname;

}
