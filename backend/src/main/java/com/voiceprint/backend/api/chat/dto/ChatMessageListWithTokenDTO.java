package com.voiceprint.backend.api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatMessageListWithTokenDTO {
    private List<ChatMessageResponseDTO> chatlog;
    private int curToken;
    private int totalToken;
}
