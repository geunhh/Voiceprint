package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.GroupInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {
    Optional<GroupInvite> findFirstByGroupIdAndExpiredAtAfter(Long groupId, LocalDateTime now);

    // 최신 그룹 초대 엔티티 조회
    Optional<GroupInvite> findTopByGroupIdOrderByCreatedAtDesc(Long groupId);

    Optional<GroupInvite> findByInviteCode(String code);
}
