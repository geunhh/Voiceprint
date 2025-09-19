package com.voiceprint.backend.user.adapter.in.web.dto;

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