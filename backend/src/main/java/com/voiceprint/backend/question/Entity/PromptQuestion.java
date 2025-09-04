package com.voiceprint.backend.question.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "prompt_questions")
public class PromptQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Byte id;

    @Column(name = "question", length = 50)
    private String question;

    @CreationTimestamp
    @Column(name= "created_at", updatable = false)
    private LocalDateTime createdAt;
}
