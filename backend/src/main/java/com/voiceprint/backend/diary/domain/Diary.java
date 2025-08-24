package com.voiceprint.backend.diary.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Diary {

    private final Integer id;
    private final String title;
    private final String content;
    private final String thumbnail;
    private final String prompt;
    private final String messages;
    private final Boolean isDeleted;
    private final Integer userId;
    private final Byte emotionId;
    private final LocalDateTime createdAt;

    @Builder
    public Diary(Integer id, String title, String content, String thumbnail, String prompt, String messages, Boolean isDeleted, Integer userId, Byte emotionId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
        this.prompt = prompt;
        this.messages = messages;
        this.isDeleted = isDeleted;
        this.userId = userId;
        this.emotionId = emotionId;
        this.createdAt = createdAt;
    }

    // 유즈케이스...
}
