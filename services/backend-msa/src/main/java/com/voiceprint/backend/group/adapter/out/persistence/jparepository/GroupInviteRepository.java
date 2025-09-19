package com.voiceprint.backend.group.adapter.out.persistence.jparepository;

import com.voiceprint.backend.group.adapter.out.persistence.GroupInviteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInviteJpaEntity, Integer> {
    Optional<GroupInviteJpaEntity> findFirstByGroupIdAndExpiredAtAfter(Integer groupId, LocalDateTime now);

    // 최신 그룹 초대 엔티티 조회
    Optional<GroupInviteJpaEntity> findTopByGroupIdOrderByCreatedAtDesc(Integer groupId);

    Optional<GroupInviteJpaEntity> findByInviteCode(String code);

    @Query("""
        select gi from GroupInvite gi
        join fetch gi.group
        where gi.inviteCode = :code
    """)
    Optional<GroupInviteJpaEntity> findByInviteCodeWithGroup(
            @Param("code") String code);
}
