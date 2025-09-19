package com.voiceprint.backend.chat.application.port.in;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatbotListResponseDTO;

public interface ChatbotUseCase {
    ChatbotListResponseDTO getChatbots(Integer userId);
}
