package com.voiceprint.backend.domain.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class DiaryThema {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "customThema")
    private User user;

    private String title;

    private String description;

    private String example;

    private String prompt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
