package com.voiceprint.backend.domain.diary;

import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.chat.ChatSession;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //채팅세션
    @OneToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;

    // 감정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id")
    private Emotion emotion;

    //제목
    @Column(length = 30, nullable = false)
    private String title;

    //내용
    @Lob
    private String content;

    //최종 여부
    @Column(name = "is_final")
    private Boolean isFinal;

    //썸네일 이미지
    @Column(length = 512)
    private String thumbnail;

    //프롬프트
    @Lob
    private String prompt;

    //삭제여부
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    //생성일시
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    //==생성 메서드==//



    //==비즈니스 로직==//


}
