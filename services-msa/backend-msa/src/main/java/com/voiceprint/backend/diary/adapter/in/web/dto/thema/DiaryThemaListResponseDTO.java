package com.voiceprint.backend.diary.adapter.in.web.dto.thema;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryThemaListResponseDTO {

    private List<DiaryThemaResponse> default_themas;
    private List<DiaryThemaResponse> custom_themas;

}
