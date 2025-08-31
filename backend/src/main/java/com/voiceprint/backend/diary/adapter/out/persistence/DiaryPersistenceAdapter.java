package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.user.adapter.in.web.dto.DiaryResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiaryPersistenceAdapter implements DiaryRepositoryPort {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;
    private final DiaryMapper diaryMapper;
    private final EntityManager em;

    @Override
    public Diary save(Diary diary) {
        // Fetch User and Emotion entities
        UserJPAEntity user = userRepository.findById(diary.getUserId())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        EmotionJPAEntity emotionEntity = null;
        if (diary.getEmotion() != null && diary.getEmotion().getId() != null) {
             emotionEntity = emotionRepository.findById(diary.getEmotion().getId())
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
    public List<DiaryResponse> findTop5DtoByUserIdOrderByCreatedAtDesc(Integer userId) {
        String jpql = "SELECT new com.voiceprint.backend.user.adapter.in.web.dto.DiaryResponse(d.id, d.title, d.content, d.createdAt, e.name) " +
                      "FROM Diary d JOIN d.emotion e " +
                      "WHERE d.user.id = :userId " +
                      "ORDER BY d.createdAt DESC";

        return em.createQuery(jpql, DiaryResponse.class)
                 .setParameter("userId", userId)
                 .setMaxResults(5)
                 .getResultList();
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
        log.info("여긴가?");
        return diaryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(diaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Diary> findById(Integer diaryId) {
        return diaryRepository.findById(diaryId).map(diaryMapper::toDomain);
    }


}
