package com.voiceprint.backend.domain.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class GroupInvite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 그룹
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    // 초대자
    @ManyToOne(fetch = FetchType.LAZY)
    private User inviter;

    // 초대 코드
    @Column(name = "invite_code", length = 16, nullable = false, unique = true)
    private String inviteCode;

    // 생성시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 만료시간
    @Column(name = "expired_at", nullable = false, updatable = false)
    private LocalDateTime expiredAt;

    //== 초대 코드 생성 메서드 ==//
    public static GroupInvite create(Group group, User inviter) {
        GroupInvite invite = new GroupInvite();
        invite.group = group;
        invite.inviter = inviter;
        invite.inviteCode = generateShortUUID();
        invite.createdAt = LocalDateTime.now();
        invite.expiredAt = invite.createdAt.plusHours(1); // 고정 1시간 추가
        return invite;
    }

    // 만료 확인 메서드
    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    // 사용 가능 확인 메서드
    public boolean isUsable() {
        return !isExpired();
    }

    // UUID - 코드 생성 메서드
    private static String generateShortUUID() {
        return UUID.randomUUID().toString().replace("-","").substring(0,14);
    }


}
