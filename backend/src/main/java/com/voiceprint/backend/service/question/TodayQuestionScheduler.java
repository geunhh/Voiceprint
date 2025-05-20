package com.voiceprint.backend.service.question;


import com.voiceprint.backend.domain.Entity.TodayQuestion;
import com.voiceprint.backend.domain.Repository.QuestionRepository;
import com.voiceprint.backend.domain.Repository.TodayQuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@Transactional
@Slf4j
public class TodayQuestionScheduler {
    private final QuestionRepository questionRepository;
    private final TodayQuestionRepository todayQuestionRepository;

    public TodayQuestionScheduler(QuestionRepository questionRepository,
                                  TodayQuestionRepository todayQuestionRepository) {
        this.questionRepository = questionRepository;
        this.todayQuestionRepository = todayQuestionRepository;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void randomTodayQuestion() {
        log.info("schedule 작동");
        // 1) 랜덤 질문 ID 조회
        Byte randomId = questionRepository.findRandomQuestionId();

        // 2) 오늘 날짜 조회
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 3)
        TodayQuestion todayQuestion = todayQuestionRepository.findByDate(today)
                .orElse(new TodayQuestion(today));

        todayQuestion.setQuestionId(randomId);
        todayQuestionRepository.save(todayQuestion);

    }
}
