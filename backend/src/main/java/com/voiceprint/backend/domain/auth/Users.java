package com.voiceprint.backend.domain.auth;
import com.voiceprint.backend.domain.chat.ChatSession;
import com.voiceprint.backend.domain.chat.Chatbot;
import com.voiceprint.backend.domain.diary.Diary;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nickname;

    @Enumerated(EnumType.STRING)

    @JoinColumn(name = "auth_provider")
    private AuthProvider authProvider;

    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 최근 사용한 테마
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "using_thema_id")
    private DiaryThema usingThema;

    // 내가 만든 커스텀 테마
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_thema_id")
    private DiaryThema customThema;

    @OneToMany(mappedBy = "user")
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ChatSession> sessions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_chatbot_id")
    private Chatbot lastChatbot;
}
