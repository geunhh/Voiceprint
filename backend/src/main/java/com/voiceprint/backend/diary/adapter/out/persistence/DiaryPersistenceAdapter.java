package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.domain.Repository.DiaryRepository;
import com.voiceprint.backend.domain.Repository.EmotionRepository;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.domain.Entity.DiaryEntity;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Entity.Emotion;
import com.voiceprint.backend.domain.model.diary.Diary;
import com.voiceprint.backend.domain.port.out.diary.DiaryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DiaryPersistenceAdapter implements DiaryRepositoryPort {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;
    private final DiaryMapper diaryMapper;

    @Override
    public Diary save(Diary diary) {
        // Fetch User and Emotion entities
        User user = userRepository.findById(diary.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Emotion emotion = emotionRepository.findById(diary.getEmotionId())
            .orElse(null); // Emotion can be optional

        DiaryEntity entity = diaryMapper.toEntity(diary, user, emotion);
        DiaryEntity savedEntity = diaryRepository.save(entity);
        return diaryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Diary> findDetailById(Integer diaryId) {
        return diaryRepository.findDetailById(diaryId).map(diaryMapper::toDomain);
    }

    @Override
    public List<Diary> findMyDiaries(Long userId, Integer cursor, int limit) {
        return diaryRepository.findMyDiaries(userId.intValue(), cursor, PageRequest.of(0, limit))
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Diary> findTop5ByUserId(Long userId) {
        return diaryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId.intValue())
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Diary> findByUserIdAndDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return diaryRepository.findByUserIdAndDateRange(userId.intValue(), start, end)
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer diaryId) {
        diaryRepository.deleteById(diaryId);
    }
}