package com.voiceprint.backend.chat.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatMessageListWithTokenDTO {
    private List<ChatMessageResponseDTO> chatlog;
    private Integer curToken;
    private Integer totalToken;
}
