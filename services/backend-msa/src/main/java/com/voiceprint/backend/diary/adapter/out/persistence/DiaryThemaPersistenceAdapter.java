package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.diary.application.port.out.DiaryThemaRepositoryPort;
import com.voiceprint.backend.diary.domain.DiaryThema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DiaryThemaPersistenceAdapter implements DiaryThemaRepositoryPort {

    private final DiaryThemaRepository diaryThemaRepository;
    private final DiaryThemaMapper diaryThemaMapper;

    @Override
    public List<DiaryThema> findByUserIdOrDefault(Integer userId) {
        return diaryThemaRepository.findByUserIdOrDefault(userId)
                .stream()
                .map(diaryThemaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DiaryThema> findByUserId(Integer userId) {
        return diaryThemaRepository.findByUserId(userId)
                .map(diaryThemaMapper::toDomain);
    }

    @Override
    public Optional<DiaryThema> findById(Integer id) {
        return diaryThemaRepository.findById(id)
                .map(diaryThemaMapper::toDomain);
    }

    @Override
    public DiaryThema save(DiaryThema diaryThema) {
        DiaryThemaJpaEntity entity = diaryThemaMapper.toEntity(diaryThema);
        DiaryThemaJpaEntity savedEntity = diaryThemaRepository.save(entity);
        return diaryThemaMapper.toDomain(savedEntity);
    }
}
