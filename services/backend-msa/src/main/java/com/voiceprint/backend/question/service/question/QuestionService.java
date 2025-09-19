package com.voiceprint.backend.question.service.question;

import com.voiceprint.backend.question.dto.QuestionGetResponseDTO;
import com.voiceprint.backend.question.Entity.PromptQuestion;
import com.voiceprint.backend.question.Repository.QuestionRepository;
import com.voiceprint.backend.question.Repository.TodayQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.NoSuchElementException;


@Service
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TodayQuestionRepository todayQuestionRepository;

    public QuestionService(QuestionRepository questionRepository,
                           TodayQuestionRepository todayQuestionRepository) {
        this.questionRepository = questionRepository;
        this.todayQuestionRepository = todayQuestionRepository;
    }
    public QuestionGetResponseDTO getTodayQuestion() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 오늘의 questionID 조회
        Byte todayQuestionId = todayQuestionRepository.findByDate(today)
                .orElseThrow(() -> new NoSuchElementException("오늘의 질문이 설정되지 않았습니다."))
                .getQuestionId();

        // 질문 리스트 조회
        PromptQuestion promptQuestion = questionRepository.getById(todayQuestionId);

        // entity -> dto
        QuestionGetResponseDTO questionGetResponseDTO = new QuestionGetResponseDTO();
        questionGetResponseDTO.setId(promptQuestion.getId());
        questionGetResponseDTO.setQuestion(promptQuestion.getQuestion());

        return questionGetResponseDTO;
    }
}
