package com.voiceprint.backend.chat.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatbotListResponseDTO {
    private Byte recentChatbotId;
    private List<ChatbotResponseDTO> chatbots;
}
