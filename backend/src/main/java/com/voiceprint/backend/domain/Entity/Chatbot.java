package com.voiceprint.backend.domain.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chatbot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Byte id;

    @Column(length = 30, nullable = false)
    private String name;

    private String description;

    @Column(length = 512)
    private String imageUrl;

    @Column(length = 100, name = "init_ment")
    private String initMent;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String prompt;

    private Boolean isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //필요하면 풀고 쓰자.
//    @OneToMany(mappedBy = "chatbot")
//    private List<ChatSession> sessions = new ArrayList<>();
}