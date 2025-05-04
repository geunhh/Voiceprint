package com.voiceprint.backend.domain.auth;

import com.voiceprint.backend.domain.chat.Chatbot;
import com.voiceprint.backend.domain.diary.Diary;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    // 최근 사용한 테마
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "using_thema_id")
    private DiaryThema usingThema;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 내가 만든 커스텀 테마
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_thema_id")
    private DiaryThema customThema;

    @OneToMany(mappedBy = "user")
    private List<Diary> diaries = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_chatbot_id")
    private Chatbot lastChatbot;
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AuthProvider {
        google, kakao
    }
}
