package com.voiceprint.backend.diary.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MonthlyEmotionResponseDTO {
    private List<EmotionCountDTO> emotions;
}
