package com.voiceprint.backend.chat.application.port.in;

import com.voiceprint.backend.chat.adapter.in.web.dto.TempDiaryResponseDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.TempDiaryUpdateRequestDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.UpdateDiaryResult;

public interface GenerateDiaryUseCase {
    void endChatSessionAndGenerateDiary(Integer userId);
    TempDiaryResponseDTO getTemporaryDiary(Integer userId);
    void retryDiaryGeneration(Integer userId);
    UpdateDiaryResult updateTemporaryDiary(Integer userId, TempDiaryUpdateRequestDTO request);
    Integer confirmDiary(Integer userId);
}
