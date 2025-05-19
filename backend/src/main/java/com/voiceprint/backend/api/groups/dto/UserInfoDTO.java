package com.voiceprint.backend.api.groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoDTO {
    private Integer id;
    private String profileImageUrl;
    private String nickname;
}