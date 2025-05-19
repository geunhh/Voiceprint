package com.voiceprint.backend.api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatTextResponseDTO {
    private String response;
    private Integer limit;
    private Integer totalToken;

}
