package com.voiceprint.backend.api.chat.dto;

import com.voiceprint.backend.domain.Entity.Chatbot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 챗봇 조회 응답 DTO
 */
@Setter
@Getter
@NoArgsConstructor
public class ChatbotResponseDTO {
    private Byte id;
    private String name;
    private String description;
    private String imageUrl;

    // 생성자 메서드
    public static ChatbotResponseDTO from(Chatbot chatbot) {
        ChatbotResponseDTO dto = new ChatbotResponseDTO();
        dto.setId(chatbot.getId());
        dto.setName(chatbot.getName());
        dto.setDescription(chatbot.getDescription());
        dto.setImageUrl(chatbot.getImageUrl());
        return dto;
    }

}
