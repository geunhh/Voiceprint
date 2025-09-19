package com.voiceprint.backend.diary.domain;

import com.voiceprint.backend.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DiaryThema {

    private final Integer id;
    private final User user;
    private final String title;
    private final String description;
    private final String example;
    private final String prompt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public DiaryThema(Integer id, User user, String title, String description, String example, String prompt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.example = example;
        this.prompt = prompt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public DiaryThema withPromptAndExample(String prompt, String example) {
        return DiaryThema.builder()
                .id(this.id)
                .user(this.user)
                .title(this.title)
                .description(this.description)
                .prompt(prompt)
                .example(example)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public DiaryThema withPrompt(String prompt) {
        return DiaryThema.builder()
                .id(this.id)
                .user(this.user)
                .title(this.title)
                .description(this.description)
                .prompt(prompt)
                .example(this.example)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
