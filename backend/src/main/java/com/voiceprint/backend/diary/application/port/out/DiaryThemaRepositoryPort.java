package com.voiceprint.backend.diary.application.port.out;

import com.voiceprint.backend.diary.domain.DiaryThema;

import java.util.List;
import java.util.Optional;

public interface DiaryThemaRepositoryPort {
    List<DiaryThema> findByUserIdOrDefault(Integer userId);
    Optional<DiaryThema> findByUserId(Integer userId);
    Optional<DiaryThema> findById(Integer id);
    DiaryThema save(DiaryThema diaryThema);
}
