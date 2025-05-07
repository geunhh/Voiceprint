package com.voiceprint.backend.domain.diary;

import io.lettuce.core.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
