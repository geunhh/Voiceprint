package com.voiceprint.backend.domain.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "users")
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프로필 이미지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private ProfileImage profileImage;


    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JoinColumn(name = "auth_provider")
    private AuthProvider authProvider;


    @Column(nullable = false)
    @Builder.Default    // builder 사용에 있어 초기화 되지 않는 문제를 해결
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();


    // 유저 생성시 자동 적용
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    // 내가 만든 커스텀 테마 (DiaryThema.user로 연결된 역방향
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private DiaryThema customThema;

    // 현재 사용중인 테마 (단방향 연관)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "using_thema_id")
    private DiaryThema usingThema;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Diary> diaries = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_chatbot_id")
    private Chatbot lastChatbot;

    // 알림 여부
    @Column(columnDefinition = "BOOLEAN")
    private Boolean enableAlarm;

    // 알람 시간 21:00 기본.
    @Column(columnDefinition = "TIME DEFAULT '21:00'")
    private LocalTime alarmTime = LocalTime.of(21,0);

    // 알림 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) //
    private List<Notification> notifications = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AuthProvider {
        google, kakao
    }

    //== 헬퍼 메서드 ==//
    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setUser(this);
    }
}
