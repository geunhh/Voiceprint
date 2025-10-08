package com.voiceprint.backend.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImage {
    private Byte id;
    private String title;
    private String imageUrl;
    private LocalDateTime createdAt;
}
