package com.voiceprint.backend.domain.diary;

import com.voiceprint.backend.domain.auth.User;

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

    //썸네일 이미지
    @Column(length = 512)
    private String thumbnail;

    //일기프롬프트
    @Lob
    private String prompt;

    //삭제여부
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    //채팅기록
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String messages;

    //생성일시
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    //==생성 메서드==//
    public static Diary createDiary(
            User user, Emotion emotion, String title, String content, String thumbnail, String prompt, String messages) {
        Diary diary = new Diary();
        diary.user = user;
        diary.emotion = emotion;
        diary.title = title;
        diary.content = content;
        diary.thumbnail = thumbnail;
        diary.prompt = prompt;
        diary.messages = messages;
        diary.createdAt = LocalDateTime.now();
        return diary;
    }




    //==비즈니스 로직==//


}
