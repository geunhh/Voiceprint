package com.voiceprint.backend.domain.ai;

import org.springframework.ai.chat.prompt.Prompt;

public interface PromptFactory {
    default Prompt buildChatPrompt(String userId, String userText) {
        throw new UnsupportedOperationException("buildChatPrompt not supported");
    }

    default Prompt buildDiaryPrompt(String userId) {
        throw new UnsupportedOperationException("buildDiaryPrompt not supported");
    }
}