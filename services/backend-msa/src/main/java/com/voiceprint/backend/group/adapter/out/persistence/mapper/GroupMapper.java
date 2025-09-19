package com.voiceprint.backend.group.adapter.out.persistence.mapper;

import com.voiceprint.backend.group.adapter.out.persistence.GroupJpaEntity;
import com.voiceprint.backend.group.domain.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public Group toDomain(GroupJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        // 재구성(reconstitute) 정적 팩토리 메서드를 사용
        return Group.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getGroupImage(),
                entity.getAlarmDays(),
                entity.getAlarmTime(),
                entity.getEnableAlarm(),
                entity.getIsDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public GroupJpaEntity toEntity(Group domain) {
        if (domain == null) {
            return null;
        }

        // 도메인 객체를 엔티티로 변환할 때는 빌더를 사용
        return GroupJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .groupImage(domain.getGroupImage())
                .alarmDays(domain.getAlarmDays())
                .alarmTime(domain.getAlarmTime())
                .enableAlarm(domain.getEnableAlarm())
                .isDeleted(domain.getIsDeleted())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
