package com.voiceprint.backend.api.chat.voice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoiceSessionResponseDto {
    private String wsUrl;
    private Long userId;
}