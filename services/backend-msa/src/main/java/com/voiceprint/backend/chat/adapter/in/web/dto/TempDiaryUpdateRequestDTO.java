package com.voiceprint.backend.chat.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 임시 다이어리 수정 API requestDTO
 */
@Getter
@NoArgsConstructor
public class TempDiaryUpdateRequestDTO {
    @NotBlank @NotNull
    private String title;
    @NotBlank @NotNull
    private String diary;
}
