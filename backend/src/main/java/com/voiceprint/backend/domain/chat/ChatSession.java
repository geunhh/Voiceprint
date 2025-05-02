package com.voiceprint.backend.domain.chat;

import com.voiceprint.backend.domain.auth.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;



    // 챗봇 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatbot_id")
    private Chatbot chatbot;

    private String messages;

    @Enumerated(EnumType.STRING)
    private ChatSessionStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
