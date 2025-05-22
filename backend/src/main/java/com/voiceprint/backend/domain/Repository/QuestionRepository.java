package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.PromptQuestion;
import com.voiceprint.backend.domain.Entity.TodayQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<PromptQuestion, Byte> {

    @Query(value = "SELECT id FROM prompt_questions ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Byte findRandomQuestionId();

}

