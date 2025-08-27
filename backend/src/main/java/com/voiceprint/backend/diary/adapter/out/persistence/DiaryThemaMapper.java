package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.diary.domain.DiaryThema;
import com.voiceprint.backend.user.adapter.out.persistence.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiaryThemaMapper {

    private final UserMapper userMapper; //

    public DiaryThema toDomain(DiaryThemaJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return DiaryThema.builder()
                .id(entity.getId())
                .user(entity.getUser() != null ? userMapper.toDomain(entity.getUser()) : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .example(entity.getExample())
                .prompt(entity.getPrompt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public DiaryThemaJpaEntity toEntity(DiaryThema domain) {
        if (domain == null) {
            return null;
        }
        UserJPAEntity userEntity = domain.getUser() != null ? userMapper.toEntity(domain.getUser()) : null;
        return DiaryThemaJpaEntity.creatDiaryThema(
            userEntity,
            domain.getTitle(),
            domain.getDescription(),
            domain.getPrompt(),
            domain.getExample()
        );
    }
}
