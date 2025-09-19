package com.voiceprint.backend.group.adapter.out.persistence.mapper;

import com.voiceprint.backend.group.adapter.out.persistence.GroupUserJpaEntity;
import com.voiceprint.backend.group.domain.GroupUser;
import com.voiceprint.backend.user.adapter.out.persistence.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupUserMapper {

    private final GroupMapper groupMapper;
    private final UserMapper userMapper;

    public GroupUser toDomain(GroupUserJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return GroupUser.builder()
                .id(entity.getId())
                .user(userMapper.toDomain(entity.getUser()))
                .group(groupMapper.toDomain(entity.getGroup()))
                .role(GroupUser.Role.valueOf(entity.getRole().name()))
                .joinedAt(entity.getJoinedAt())
                .build();
    }

    public GroupUserJpaEntity toEntity(GroupUser domain) {
        if (domain == null) {
            return null;
        }

        return GroupUserJpaEntity.builder()
                .id(domain.getId())
                .user(userMapper.toEntity(domain.getUser()))
                .group(groupMapper.toEntity(domain.getGroup()))
                .role(GroupUserJpaEntity.Role.valueOf(domain.getRole().name()))
                .joinedAt(domain.getJoinedAt())
                .build();
    }
}
