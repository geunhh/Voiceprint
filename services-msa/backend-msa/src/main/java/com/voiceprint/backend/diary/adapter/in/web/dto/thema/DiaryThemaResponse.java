package com.voiceprint.backend.diary.adapter.in.web.dto.thema;

import com.voiceprint.backend.diary.adapter.out.persistence.DiaryThemaJpaEntity;
import com.voiceprint.backend.diary.domain.DiaryThema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiaryThemaResponse {
    private Integer id;
    private String title;
    private String description;
    private String example;

    public static DiaryThemaResponse from(DiaryThemaJpaEntity thema) {
        return new DiaryThemaResponse(
                thema.getId(),
                thema.getTitle(),
                thema.getDescription(),
                thema.getExample()
        );
    }

    public static DiaryThemaResponse fromDomain(DiaryThema thema) {
        return new DiaryThemaResponse(
                thema.getId(),
                thema.getTitle(),
                thema.getDescription(),
                thema.getExample()
        );
    }
}
