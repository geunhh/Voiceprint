package com.voiceprint.backend.api.question.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuestionGetResponseDTO {
    private byte id;
    private String question;
}
