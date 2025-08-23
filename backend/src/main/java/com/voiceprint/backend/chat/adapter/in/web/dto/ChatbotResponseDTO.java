package com.voiceprint.backend.chat.adapter.in.web.dto;

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

}
