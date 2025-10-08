package com.voiceprint.backend.diary.domain;

import com.voiceprint.backend.chat.domain.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class Diary {

    private final Integer id;
    private final String title;
    private final String content;
    private final String thumbnail;
    private final String prompt;
    private final List<ChatMessage> messages;
    private final Boolean isDeleted;
    private final Integer userId;
    private final Emotion emotion;
    private final LocalDateTime createdAt;

    @Builder
    public Diary(Integer id, String title, String content, String thumbnail, String prompt, List<ChatMessage> messages,
                 Boolean isDeleted, Integer userId, Emotion emotion, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
        this.prompt = prompt;
        this.messages = messages;
        this.isDeleted = isDeleted;
        this.userId = userId;
        this.emotion = emotion;
        this.createdAt = createdAt;
    }

    // 유즈케이스...
}
