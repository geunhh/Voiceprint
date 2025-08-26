package com.voiceprint.backend.chat.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatMessage {

    private final String role;
    private final String content;

    @Builder
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}