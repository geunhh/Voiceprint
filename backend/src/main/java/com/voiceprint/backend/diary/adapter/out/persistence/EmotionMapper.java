package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.diary.domain.Emotion;
import org.springframework.stereotype.Component;

@Component
public class EmotionMapper {

    public Emotion toDomain(EmotionJPAEntity entity) {
        if (entity == null) return null;
        return Emotion.builder()
                .id(entity.getId())
                .name(entity.getName())
                .color(entity.getColor())
                .build();
    }

    public EmotionJPAEntity toEntity(Emotion domain) {
        if (domain == null) return null;
        return EmotionJPAEntity.create(
                domain.getName(),
                domain.getColor());
    }
}
