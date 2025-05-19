package com.voiceprint.backend.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileUpdateResponse {
    private Integer userId;
    private String nickname;
    private Byte profileImageId;
}