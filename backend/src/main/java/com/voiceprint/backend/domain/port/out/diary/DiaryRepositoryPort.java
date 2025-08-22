package com.voiceprint.backend.domain.port.out.diary;

import com.voiceprint.backend.domain.model.diary.Diary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryRepositoryPort {
    Diary save(Diary diary);
    Optional<Diary> findDetailById(Integer diaryId);
    List<Diary> findMyDiaries(Long userId, Integer cursor, int limit);
    List<Diary> findTop5ByUserId(Long userId);
    List<Diary> findByUserIdAndDateRange(Long userId, LocalDateTime start, LocalDateTime end);
    void deleteById(Integer diaryId);
}