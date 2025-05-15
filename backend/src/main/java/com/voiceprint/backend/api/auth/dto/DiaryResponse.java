package com.voiceprint.backend.api.auth.dto;

import com.voiceprint.backend.domain.Entity.Diary;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
//@AllArgsConstructor
@Slf4j
public class DiaryResponse {
    private Long diaryId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String emotion;

    public DiaryResponse(Diary diary) {
        this.diaryId = diary.getId();
        this.title = diary.getTitle();
        this.content = diary.getContent();
        this.createdAt = diary.getCreatedAt();
        this.emotion = diary.getEmotion().getName();
    }
}
