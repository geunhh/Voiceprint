package com.voiceprint.backend.api.auth.dto;

import com.voiceprint.backend.domain.Entity.DiaryEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
//@AllArgsConstructor
@Slf4j
public class DiaryResponse {
    private Integer diaryId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String emotion;

    public DiaryResponse(DiaryEntity diaryEntity) {
        this.diaryId = diaryEntity.getId();
        this.title = diaryEntity.getTitle();
        this.content = diaryEntity.getContent();
        this.createdAt = diaryEntity.getCreatedAt();
        this.emotion = diaryEntity.getEmotion().getName();
    }
}
