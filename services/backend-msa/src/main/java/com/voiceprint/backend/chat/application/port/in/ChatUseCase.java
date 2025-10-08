package com.voiceprint.backend.chat.application.port.in;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatTextResponseDTO;

public interface ChatUseCase {
    ChatTextResponseDTO processChat(Integer userId, String message);
}
