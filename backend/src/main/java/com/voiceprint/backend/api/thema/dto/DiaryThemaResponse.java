package com.voiceprint.backend.api.thema.dto;

import com.voiceprint.backend.domain.Entity.DiaryThema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiaryThemaResponse {
    private Long id;
    private String title;
    private String description;
    private String example;

    public static DiaryThemaResponse from(DiaryThema thema) {
        return new DiaryThemaResponse(
                thema.getId(),
                thema.getTitle(),
                thema.getDescription(),
                thema.getExample()
        );
    }
}
