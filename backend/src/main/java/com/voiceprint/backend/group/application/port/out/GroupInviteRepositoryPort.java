package com.voiceprint.backend.group.application.port.out;

import com.voiceprint.backend.group.domain.GroupInvite;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GroupInviteRepositoryPort {

    /**
     * @deprecated Use createInvite instead for better performance and clarity.
     */
    @Deprecated
    GroupInvite save(GroupInvite groupInvite);

    GroupInvite createInvite(Integer groupId, Integer inviterId);

    Optional<GroupInvite> findFirstByGroupIdAndExpiredAtAfter(Integer groupId, LocalDateTime now);

    Optional<GroupInvite> findTopByGroupIdOrderByCreatedAtDesc(Integer groupId);

    Optional<GroupInvite> findByInviteCode(String code);

    Optional<GroupInvite> findByInviteCodeWithGroup(String code);
}
