package com.voiceprint.backend.domain.port.in.diary;

import com.voiceprint.backend.api.chat.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.api.diary.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.api.diary.dto.DiaryListWithCursorDTO;
import com.voiceprint.backend.api.diary.dto.DiaryMontlyListDTO;

import java.util.List;

public interface DiaryUseCase {
    DiaryDetailResponseDTO getDiaryDetail(Integer userId, Integer diaryId);
    DiaryListWithCursorDTO getUserDiaries(Integer userId, Integer cursor, Integer size);
    DiaryMontlyListDTO getMonthlyDiaries(Integer userId, int year, int month);
    List<ChatMessageResponseDTO> getChatRecordFromDiary(Integer userId, Integer diaryId);

    //TODO : 수정 필요.
    /**
     * 근데 클린/헥사고날을 엄격하게 지키려면,
     * UseCase가 DTO 또한 몰라야 함.
     * 순수한 도메인 객체를 반환해야 함. Diary.
     *
     * - 애플리케이션 계층이 presentation 계층에 종속되게 됨.
     */
}