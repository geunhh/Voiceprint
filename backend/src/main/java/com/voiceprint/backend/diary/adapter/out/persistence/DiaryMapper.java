package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.diary.domain.Diary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiaryMapper {

    private final EmotionMapper emotionMapper;

    public Diary toDomain(DiaryJpaEntity entity) {
        return Diary.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .thumbnail(entity.getThumbnail())
                .prompt(entity.getPrompt())
                .messages(entity.getMessages())
                .isDeleted(entity.getIsDeleted())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .emotion(emotionMapper.toDomain(entity.getEmotion()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public DiaryJpaEntity toEntity(Diary domain, UserJPAEntity user, EmotionJPAEntity emotion) {
        // The User and Emotion entities are fetched in the adapter and passed here.
        return DiaryJpaEntity.createDiary(
            user,
            emotion,
            domain.getTitle(),
            domain.getContent(),
            domain.getThumbnail(),
            domain.getPrompt(),
            domain.getMessages()
        );
    }
}