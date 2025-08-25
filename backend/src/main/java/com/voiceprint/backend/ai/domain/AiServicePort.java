package com.voiceprint.backend.ai.domain;

import org.springframework.ai.chat.prompt.Prompt;

public interface AiServicePort {

    AiResult chat(String userId, String userMessage);
    AiResult chat(Prompt prompt);
}
