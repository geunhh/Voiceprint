package com.voiceprint.backend.diary.application.port.out;

import com.voiceprint.backend.diary.domain.Diary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryRepositoryPort {
    Diary save(Diary diary);
    Optional<Diary> findDetailById(Integer diaryId);
    List<Diary> findMyDiaries(Integer userId, Integer cursor, int limit);
    List<Diary> findTop5ByUserId(Integer userId);
    List<Diary> findByUserIdAndDateRange(Integer userId, LocalDateTime start, LocalDateTime end);
    void deleteById(Integer diaryId);
    List<Diary> findByUserIdAndCreatedAtBetween(Integer userId, LocalDateTime start, LocalDateTime end);
    List<Diary> findTop5ByUserIdOrderByCreatedAtDesc(Integer userId);
}