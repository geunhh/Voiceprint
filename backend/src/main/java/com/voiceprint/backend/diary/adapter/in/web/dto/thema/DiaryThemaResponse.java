package com.voiceprint.backend.diary.adapter.in.web.dto.thema;

import com.voiceprint.backend.diary.adapter.out.persistence.DiaryThema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiaryThemaResponse {
    private Integer id;
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
