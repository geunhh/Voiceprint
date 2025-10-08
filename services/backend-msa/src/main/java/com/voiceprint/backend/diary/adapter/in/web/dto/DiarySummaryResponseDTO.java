package com.voiceprint.backend.diary.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiarySummaryResponseDTO {
    private Integer diaryId;
    private String title;
    private String content;
    private String emotion;
    private String createdAt;
    private String thumbnail;

}
