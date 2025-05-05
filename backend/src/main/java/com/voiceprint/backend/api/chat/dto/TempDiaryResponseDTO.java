package com.voiceprint.backend.api.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TempDiaryResponseDTO {
    private String title;
    private String diary;
    private String createdAt;
    private String emotion;
}
