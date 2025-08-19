package com.voiceprint.backend.api.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WeeklyEmotionResponseDTO {
    private List<String> emotions;
}
