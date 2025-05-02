package com.voiceprint.backend.api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅세션 메시지 조회 DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class ChatMessageResponseDTO {
    private String role;
    private String message;



}
