package com.voiceprint.backend.domain;

import com.voiceprint.backend.domain.auth.User;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "groups_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupUser {

    // 복합키 지정 어노테이션
    @EmbeddedId
    private GroupUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_gu_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "fk_gu_group"))
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private Boolean enableAlarm = false;

    private LocalDateTime alarm;

    public enum Role {
        ADMIN,   // 그룹 관리자
        MEMBER   // 일반 사용자
    }
}
