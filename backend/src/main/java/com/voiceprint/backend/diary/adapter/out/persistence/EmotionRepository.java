package com.voiceprint.backend.diary.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmotionRepository extends JpaRepository<Emotion, Byte> {

    Optional<Emotion> findByName(String name);
}
