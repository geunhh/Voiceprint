package com.voiceprint.backend.group.adapter.out.persistence;

import com.voiceprint.backend.group.adapter.out.persistence.jparepository.GroupUserRepository;
import com.voiceprint.backend.group.adapter.out.persistence.mapper.GroupUserMapper;
import com.voiceprint.backend.group.application.port.out.GroupUserRepositoryPort;
import com.voiceprint.backend.group.domain.GroupUser;
import com.voiceprint.backend.user.adapter.out.persistence.UserMapper;
import com.voiceprint.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GroupUserPersistenceAdapter implements GroupUserRepositoryPort {

    private final GroupUserRepository groupUserRepository;
    private final GroupUserMapper groupUserMapper;
    private final UserMapper userMapper;

    @Override
    public GroupUser save(GroupUser groupUser) {
        GroupUserJpaEntity entity = groupUserMapper.toEntity(groupUser);
        GroupUserJpaEntity savedEntity = groupUserRepository.save(entity);
        return groupUserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<GroupUser> findByGroupIdAndUserId(Integer groupId, Integer userId) {
        return groupUserRepository.findByGroupIdAndUserId(groupId, userId)
                .map(groupUserMapper::toDomain);
    }

    @Override
    public List<GroupUser> findAllByGroupId(Integer groupId) {
        return groupUserRepository.findAllByGroupId(groupId).stream()
                .map(groupUserMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndGroupId(Integer userId, Integer groupId) {
        return groupUserRepository.existsByUserIdAndGroupId(userId,groupId);
    }

    @Override
    public List<Integer> findGroupIdsByUserId(Integer userId) {
        return groupUserRepository.findGroupIdsByUserId(userId);
    }

    @Override
    public List<User> findUsersByGroupId(Integer groupId) {
        return groupUserRepository.findUsersByGroupId(groupId).stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }
}
