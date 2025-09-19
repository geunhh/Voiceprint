package com.voiceprint.backend.question.Repository;

import com.voiceprint.backend.question.Entity.PromptQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<PromptQuestion, Byte> {

    @Query(value = "SELECT id FROM prompt_questions ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Byte findRandomQuestionId();

}

