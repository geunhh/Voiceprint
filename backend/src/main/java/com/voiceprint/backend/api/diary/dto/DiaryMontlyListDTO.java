package com.voiceprint.backend.api.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryMontlyListDTO {
    private List<DiarySummaryResponseDTO> diaries;
}
