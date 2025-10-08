package com.voiceprint.backend.chat.application.port.in;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageListWithTokenDTO;
import com.voiceprint.backend.chat.domain.ChatSessionStatus;

public interface ChatSessionUseCase {
    void startChatSession(Integer userId, Byte chatbotId);
    ChatSessionStatus getChatSessionStatus(Integer userId);
    ChatMessageListWithTokenDTO getChatHistory(long userId);
}