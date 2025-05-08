package com.voiceprint.backend.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileUpdateResponse {
    private Long userId;
    private String nickname;
    private Long profileImageId;
}