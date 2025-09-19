package com.voiceprint.backend.group.adapter.out.persistence;

import com.voiceprint.backend.group.adapter.out.persistence.jparepository.GroupRepository;
import com.voiceprint.backend.group.adapter.out.persistence.mapper.GroupMapper;
import com.voiceprint.backend.group.application.port.out.GroupRepositoryPort;
import com.voiceprint.backend.group.domain.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GroupPersistenceAdapter implements GroupRepositoryPort {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    @Override
    public Group save(Group group) {
        GroupJpaEntity entity = groupMapper.toEntity(group);
        GroupJpaEntity savedEntity = groupRepository.save(entity);
        return groupMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Group> findById(Integer id) {
        return groupRepository.findById(id)
                .map(groupMapper::toDomain);
    }

    @Override
    public List<Group> findAllByUserId(Integer userId) {
        return groupRepository.findAllByUserId(userId).stream()
                .map(groupMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer id) {
        groupRepository.deleteById(id);
    }
}
