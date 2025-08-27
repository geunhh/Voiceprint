package com.voiceprint.backend.diary.application.port.in;

import com.voiceprint.backend.diary.adapter.in.web.dto.thema.DiaryThemaCreateResponse;
import com.voiceprint.backend.diary.adapter.in.web.dto.thema.DiaryThemaListResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.thema.UsingDiaryThemaResponseDTO;

public interface DiaryThemaUseCase {
    DiaryThemaListResponseDTO getThemasForUser(Integer userId);
    void selectThema(Integer userId, Integer themaId);
    DiaryThemaCreateResponse createCustomThema(Integer userId, String exampleDiary);
    void updateCustomThemaFromDiary(Integer userId, Integer diaryId);
    UsingDiaryThemaResponseDTO getUsingThema(Integer userId);
}
