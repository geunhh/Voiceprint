package com.voiceprint.backend.chat.adapter.in.web.voice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoiceSessionResponseDto {
    private String wsUrl;
    private Integer userId;
}