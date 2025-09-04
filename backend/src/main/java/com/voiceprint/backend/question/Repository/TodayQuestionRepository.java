package com.voiceprint.backend.question.Repository;

import com.voiceprint.backend.question.Entity.TodayQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TodayQuestionRepository extends JpaRepository<TodayQuestion, LocalDate> {
    Optional<TodayQuestion> findByDate(LocalDate date);
}
