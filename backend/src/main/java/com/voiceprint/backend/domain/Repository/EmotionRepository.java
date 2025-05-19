package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmotionRepository extends JpaRepository<Emotion, Byte> {

    Optional<Emotion> findByName(String name);
}
