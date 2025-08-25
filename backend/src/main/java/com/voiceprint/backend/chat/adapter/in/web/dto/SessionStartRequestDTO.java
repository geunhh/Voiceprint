package com.voiceprint.backend.chat.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionStartRequestDTO {
    @NotNull
    private Byte chatbotId;
}
