package com.voiceprint.backend.diary.application.port.out;

import com.voiceprint.backend.diary.domain.Diary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryRepositoryPort {
    Optional<Diary> findDetailById(Integer diaryId);
    List<Diary> findMyDiaries(Integer userId, Integer cursor, int limit);
    List<Diary> findTop5ByUserId(Integer userId);
    List<Diary> findByUserIdAndDateRange(Integer userId, LocalDateTime start, LocalDateTime end);
    List<Diary> findByUserIdAndCreatedAtBetween(Integer userId, LocalDateTime start, LocalDateTime end);
    List<Diary> findTop5ByUserIdOrderByCreatedAtDesc(Integer userId);

    Optional<Diary> findById(Integer diaryId);
    void deleteById(Integer diaryId);
    Diary save(Diary diary);

}