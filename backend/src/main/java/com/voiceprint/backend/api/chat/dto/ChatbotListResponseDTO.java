package com.voiceprint.backend.api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatbotListResponseDTO {
    private Byte recentChatbotId;
    private List<ChatbotResponseDTO> chatbots;
}
