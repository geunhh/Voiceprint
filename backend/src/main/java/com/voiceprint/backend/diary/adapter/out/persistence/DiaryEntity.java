package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.global.util.ChatMessageListConverter;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageResponseDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "Diary") // JPQL 엔티티명 = Diary
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJPAEntity user;

    // 감정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id")
    private EmotionJPAEntity emotion;

    //제목
    @Column(length = 30, nullable = false)
    private String title;

    //내용
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    //썸네일 이미지
    @Column(length = 512)
    private String thumbnail;

    //일기프롬프트
    @Lob
    @Column(columnDefinition = "TEXT")
    private String prompt;

    //삭제여부
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    //채팅기록
    @Convert(converter = ChatMessageListConverter.class)
    @Column(columnDefinition = "json")
    private List<ChatMessageResponseDTO> messages;

    //생성일시
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    //==생성 메서드==//
    public static DiaryEntity createDiary(
            UserJPAEntity user, EmotionJPAEntity emotion, String title, String content, String thumbnail, String prompt, List<ChatMessageResponseDTO> messages) {
        DiaryEntity diaryEntity = new DiaryEntity();
        diaryEntity.user = user;
        diaryEntity.emotion = emotion;
        diaryEntity.title = title;
        diaryEntity.content = content;
        diaryEntity.thumbnail = thumbnail;
        diaryEntity.prompt = prompt;
        diaryEntity.messages = messages;
        diaryEntity.createdAt = LocalDateTime.now();
        return diaryEntity;
    }



    //==비즈니스 로직==//


}
