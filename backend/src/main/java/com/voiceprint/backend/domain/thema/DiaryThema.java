package com.voiceprint.backend.domain.thema;

import com.voiceprint.backend.domain.auth.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "diary_themas")
public class DiaryThema {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(length = 10)
    private String title;

    @Column(length = 100)
    private String description;

    private String example;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //== 생성메서드 ==//
    public static DiaryThema creatDiaryThema(
        User user, String title, String description, String prompt, String example
    ) {
        DiaryThema thema = new DiaryThema();
        thema.setUser(user);
        thema.setPrompt(prompt);
        thema.setExample(example);
        thema.setTitle("나만의 테마");         // 또는 "나만의 테마" 기본값 줄 수도 있음
        thema.setDescription("사용자 입력 기반 테마");   // 또는 "사용자 입력 기반" 기본값도 가능
        return thema;

    }
}
