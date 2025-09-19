package com.voiceprint.backend.diary.application.port.out;

import com.voiceprint.backend.diary.domain.Emotion;

import java.util.Optional;

public interface EmotionRepositoryPort {
    Optional<Emotion> findByName(String name);
}
