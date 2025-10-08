package com.voiceprint.backend.group.domain;

import com.voiceprint.backend.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GroupUser {

    private final GroupUserId id;
    private final User user;
    private final Group group;
    private final Role role;
    private final LocalDateTime joinedAt;

    public enum Role {
        ADMIN,
        MEMBER
    }

    @Builder(toBuilder = true)
    public GroupUser(GroupUserId id, User user, Group group, Role role, LocalDateTime joinedAt) { // UserJpaEntity -> User
        this.id = id;
        this.user = user;
        this.group = group;
        this.role = role;
        this.joinedAt = joinedAt;
    }
}