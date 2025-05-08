package com.voiceprint.backend.api.auth.dto;

import com.voiceprint.backend.domain.diary.Diary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Slf4j
@AllArgsConstructor
public class ProfileResponse {
    private Long userId;
    private String nickname;
    private String imageUrl;
    private List<DiaryResponse> diaries;

}
