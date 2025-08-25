package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
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
        UserJPAEntity user = userRepository.findById(diary.getUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        EmotionJPAEntity emotionEntity = null;
        if (diary.getEmotion() != null && diary.getEmotion().getId() != null) {
             emotionEntity = emotionRepository.findById(emotionEntity.getId())
                    .orElse(null);
        }

        DiaryEntity entity = diaryMapper.toEntity(diary, user, emotionEntity);
        DiaryEntity savedEntity = diaryRepository.save(entity);
        return diaryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Diary> findDetailById(Integer diaryId) {
        return diaryRepository.findDetailById(diaryId).map(diaryMapper::toDomain);
    }

    @Override
    public List<Diary> findMyDiaries(Integer userId, Integer cursor, int limit) {
        return diaryRepository.findMyDiaries(userId, cursor, PageRequest.of(0, limit))
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Diary> findTop5ByUserId(Integer userId) {
        return diaryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Diary> findByUserIdAndDateRange(Integer userId, LocalDateTime start, LocalDateTime end) {
        return diaryRepository.findByUserIdAndDateRange(userId, start, end)
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Integer diaryId) {
        diaryRepository.deleteById(diaryId);
    }

    @Override
    public List<Diary> findByUserIdAndCreatedAtBetween(Integer userId, LocalDateTime start, LocalDateTime end) {
        return diaryRepository.findByUserIdAndCreatedAtBetween(userId, start, end)
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Diary> findTop5ByUserIdOrderByCreatedAtDesc(Integer userId) {
        return diaryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }


}