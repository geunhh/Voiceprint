package com.voiceprint.backend.diary.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiaryDetailResponseDTO {
    private Integer diaryId;
    private String title;
    private String content;
    private String emotion;
    private String createdAt;        // ISO 8601 string
    private String authorNickname;
    private String thumbnail;        // nullable
}
