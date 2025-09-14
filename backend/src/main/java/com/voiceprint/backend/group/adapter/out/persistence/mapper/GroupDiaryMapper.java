package com.voiceprint.backend.group.adapter.out.persistence.mapper;

import com.voiceprint.backend.diary.adapter.out.persistence.DiaryJpaEntity;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryMapper;
import com.voiceprint.backend.group.adapter.out.persistence.GroupDiaryJpaEntity;
import com.voiceprint.backend.group.adapter.out.persistence.GroupJpaEntity;
import com.voiceprint.backend.group.domain.GroupDiary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupDiaryMapper {

    private final DiaryMapper diaryMapper;
    private final GroupMapper groupMapper;

    public GroupDiary toDomain(GroupDiaryJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return GroupDiary.builder()
                .id(entity.getId())
                .diary(diaryMapper.toDomain(entity.getDiary()))
                .group(groupMapper.toDomain(entity.getGroup()))
                .sharedAt(entity.getSharedAt())
                .build();
    }

    public GroupDiaryJpaEntity toEntity(GroupDiary domain, DiaryJpaEntity diaryEntity, GroupJpaEntity groupEntity) {
        if (domain == null) {
            return null;
        }

        return GroupDiaryJpaEntity.builder()
                .id(domain.getId())
                .diary(diaryEntity)
                .group(groupEntity)
                .sharedAt(domain.getSharedAt())
                .build();
    }
}
