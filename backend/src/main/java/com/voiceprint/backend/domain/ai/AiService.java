package com.voiceprint.backend.domain.ai;

import org.springframework.ai.chat.prompt.Prompt;

public interface AiService {

    AiResult chat(String userId, String userMessage);
    AiResult chat(Prompt prompt);
}
