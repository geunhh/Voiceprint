package com.voiceprint.backend.api.thema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryThemaListResponseDTO {

    private List<DiaryThemaResponse> default_themas;
    private List<DiaryThemaResponse> custom_themas;

}
