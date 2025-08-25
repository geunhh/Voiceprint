package com.voiceprint.backend.group.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoDTO {
    private Integer id;
    private String profileImageUrl;
    private String nickname;
}