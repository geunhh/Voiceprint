package com.voiceprint.backend.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Slf4j
@AllArgsConstructor
public class ProfileResponse {
    private Integer userId;
    private String nickname;
    private String imageUrl;
    private List<DiaryResponse> diaries;

}
