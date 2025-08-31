package com.voiceprint.backend.group.domain;

import com.voiceprint.backend.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class GroupInvite {

    private final Integer id;
    private final Group group;
    private final User inviter;
    private final String inviteCode;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiredAt;

    @Builder(toBuilder = true)
    public GroupInvite(Integer id, Group group, User inviter, String inviteCode, LocalDateTime createdAt, LocalDateTime expiredAt) {
        this.id = id;
        this.group = group;
        this.inviter = inviter;
        this.inviteCode = inviteCode;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
    }

    // Static factory method for creation (moved from JPA entity)
    public static GroupInvite create(Group group, User inviter) {
        // Note: id will be null for new invites, set by persistence later
        return GroupInvite.builder()
                .group(group)
                .inviter(inviter)
                .inviteCode(generateShortUUID())
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusHours(1)) // Fixed 1 hour
                .build();
    }

    // Domain methods (moved from JPA entity)
    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public boolean isUsable() {
        return !isExpired();
    }

    // Helper method (moved from JPA entity)
    private static String generateShortUUID() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }
}
