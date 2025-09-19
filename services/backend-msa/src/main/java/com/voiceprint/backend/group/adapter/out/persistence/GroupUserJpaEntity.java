package com.voiceprint.backend.group.adapter.out.persistence;

import com.voiceprint.backend.group.domain.GroupUserId;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "GroupUser")
@Table(name = "groups_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupUserJpaEntity {

    // 복합키 지정 어노테이션
    @EmbeddedId
    private GroupUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_gu_user"))
    private UserJPAEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "fk_gu_group"))
    private GroupJpaEntity group;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public enum Role {
        ADMIN,   // 그룹 관리자
        MEMBER   // 일반 사용자
    }
}
