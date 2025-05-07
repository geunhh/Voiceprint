package com.voiceprint.backend.api.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiarySummaryResponseDTO {
    private Long diaryId;
    private String title;
    private String content;
    private String emotion;
    private String createdAt;
    private String thumbnail;

}
