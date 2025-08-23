package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.domain.Entity.DiaryEntity;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Entity.Emotion;
import com.voiceprint.backend.domain.model.diary.Diary;
import org.springframework.stereotype.Component;

@Component
public class DiaryMapper {

    public Diary toDomain(DiaryEntity entity) {
        return Diary.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .thumbnail(entity.getThumbnail())
                .prompt(entity.getPrompt())
                .messages(entity.getMessages())
                .isDeleted(entity.getIsDeleted())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .emotionId(entity.getEmotion() != null ? entity.getEmotion().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public DiaryEntity toEntity(Diary domain, User user, Emotion emotion) {
        // The User and Emotion entities are fetched in the adapter and passed here.
        return DiaryEntity.createDiary(
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