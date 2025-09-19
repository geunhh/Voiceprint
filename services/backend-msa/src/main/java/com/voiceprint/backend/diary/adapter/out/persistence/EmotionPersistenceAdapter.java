package com.voiceprint.backend.diary.adapter.out.persistence;

import com.voiceprint.backend.diary.application.port.out.EmotionRepositoryPort;
import com.voiceprint.backend.diary.domain.Emotion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmotionPersistenceAdapter implements EmotionRepositoryPort {

    private final EmotionRepository emotionRepository;
    private final EmotionMapper emotionMapper;

    @Override
    public Optional<Emotion> findByName(String name) {
        return emotionRepository.findByName(name)
                .map(emotionMapper::toDomain);
    }
}
