package com.voiceprint.backend.group.adapter.out.persistence.mapper;

import com.voiceprint.backend.group.adapter.out.persistence.GroupInviteJpaEntity;
import com.voiceprint.backend.group.adapter.out.persistence.GroupJpaEntity;
import com.voiceprint.backend.group.domain.GroupInvite;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.user.adapter.out.persistence.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupInviteMapper {

    private final GroupMapper groupMapper;
    private final UserMapper userMapper;

    public GroupInvite toDomain(GroupInviteJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return GroupInvite.builder()
                .id(entity.getId())
                .group(groupMapper.toDomain(entity.getGroup()))
                .inviter(userMapper.toDomain(entity.getInviter()))
                .inviteCode(entity.getInviteCode())
                .createdAt(entity.getCreatedAt())
                .expiredAt(entity.getExpiredAt())
                .build();
    }

    public GroupInviteJpaEntity toEntity(GroupInvite domain, GroupJpaEntity groupEntity, UserJPAEntity inviterEntity) {
        if (domain == null) {
            return null;
        }

        return GroupInviteJpaEntity.builder()
                .id(domain.getId())
                .group(groupEntity)
                .inviter(inviterEntity)
                .inviteCode(domain.getInviteCode())
                .createdAt(domain.getCreatedAt())
                .expiredAt(domain.getExpiredAt())
                .build();
    }
}
