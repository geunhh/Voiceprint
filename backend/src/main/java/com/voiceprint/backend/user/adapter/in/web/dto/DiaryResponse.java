package com.voiceprint.backend.user.adapter.in.web.dto;

import com.voiceprint.backend.diary.domain.Diary;
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

    public DiaryResponse(Diary diary) {
        this.diaryId = diary.getId();
        this.title = diary.getTitle();
        this.content = diary.getContent();
        this.createdAt = diary.getCreatedAt();
        this.emotion = diary.getEmotion().getName();
    }
}
