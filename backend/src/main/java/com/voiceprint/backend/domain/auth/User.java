package com.voiceprint.backend.domain.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "users")
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer profileImageId;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    private Integer usingThema;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AuthProvider {
        google, kakao
    }
}
