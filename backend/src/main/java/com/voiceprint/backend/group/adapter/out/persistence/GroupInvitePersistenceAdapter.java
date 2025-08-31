package com.voiceprint.backend.group.adapter.out.persistence;

import com.voiceprint.backend.group.adapter.out.persistence.jparepository.GroupInviteRepository;
import com.voiceprint.backend.group.adapter.out.persistence.mapper.GroupInviteMapper;
import com.voiceprint.backend.group.application.port.out.GroupInviteRepositoryPort;
import com.voiceprint.backend.group.domain.GroupInvite;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupInvitePersistenceAdapter implements GroupInviteRepositoryPort {

    private final GroupInviteRepository groupInviteRepository;
    private final GroupInviteMapper groupInviteMapper;
    private final EntityManager em;

    /**
     * @deprecated Use createInvite instead for better performance and clarity.
     */
    @Override
    @Deprecated
    public GroupInvite save(GroupInvite groupInvite) {
        throw new UnsupportedOperationException("This method is deprecated. Use createInvite() instead.");
    }

    @Override
    public GroupInvite createInvite(Integer groupId, Integer inviterId) {
        // 영
        GroupJpaEntity groupRef = em.getReference(GroupJpaEntity.class, groupId);
        UserJPAEntity inviterRef = em.getReference(UserJPAEntity.class, inviterId);

        GroupInviteJpaEntity newInvite = GroupInviteJpaEntity.create(groupRef, inviterRef);

        GroupInviteJpaEntity savedEntity = groupInviteRepository.save(newInvite);
        return groupInviteMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<GroupInvite> findFirstByGroupIdAndExpiredAtAfter(Integer groupId, LocalDateTime now) {
        return groupInviteRepository.findFirstByGroupIdAndExpiredAtAfter(groupId, now)
                .map(groupInviteMapper::toDomain);
    }

    @Override
    public Optional<GroupInvite> findTopByGroupIdOrderByCreatedAtDesc(Integer groupId) {
        return groupInviteRepository.findTopByGroupIdOrderByCreatedAtDesc(groupId)
                .map(groupInviteMapper::toDomain);
    }

    @Override
    public Optional<GroupInvite> findByInviteCode(String code) {
        return groupInviteRepository.findByInviteCode(code)
                .map(groupInviteMapper::toDomain);
    }

    @Override
    public Optional<GroupInvite> findByInviteCodeWithGroup(String code) {
        return groupInviteRepository.findByInviteCodeWithGroup(code)
                .map(groupInviteMapper::toDomain);
    }
}
