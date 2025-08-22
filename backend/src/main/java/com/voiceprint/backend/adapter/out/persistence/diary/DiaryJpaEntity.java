package com.voiceprint.backend.adapter.out.persistence.diary;

import com.voiceprint.backend.domain.Entity.Emotion;
import com.voiceprint.backend.domain.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 제거 예정
 */
//@Entity
@Table(name = "Diary")
@Getter
@Setter // 매핑을 위해 Setter를 추가합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id")
    private Emotion emotion;

    @Column(length = 30, nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 512)
    private String thumbnail;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(columnDefinition = "json")
    private String messages;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DiaryJpaEntity(Integer id, User user, Emotion emotion, String title, String content, String thumbnail, String prompt, Boolean isDeleted, String messages, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.emotion = emotion;
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
        this.prompt = prompt;
        this.isDeleted = isDeleted;
        this.messages = messages;
        this.createdAt = createdAt;
    }
}
