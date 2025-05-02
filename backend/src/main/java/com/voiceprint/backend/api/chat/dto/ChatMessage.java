package com.voiceprint.backend.api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Redis 메시지 주고받을 때 직렬화/역직렬화
 */
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;
    private String message;
}
