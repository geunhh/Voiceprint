package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.GroupInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Integer> {
    Optional<GroupInvite> findFirstByGroupIdAndExpiredAtAfter(Integer groupId, LocalDateTime now);

    // 최신 그룹 초대 엔티티 조회
    Optional<GroupInvite> findTopByGroupIdOrderByCreatedAtDesc(Integer groupId);

    Optional<GroupInvite> findByInviteCode(String code);

    @Query("""
        select gi from GroupInvite gi
        join fetch gi.group
        where gi.inviteCode = :code
    """)
    Optional<GroupInvite> findByInviteCodeWithGroup(
            @Param("code") String code);
}
