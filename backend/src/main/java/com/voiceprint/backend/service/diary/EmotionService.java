package com.voiceprint.backend.service.diary;

import com.voiceprint.backend.api.diary.dto.EmotionCountDTO;
import com.voiceprint.backend.api.diary.dto.MonthlyEmotionResponseDTO;
import com.voiceprint.backend.api.diary.dto.WeeklyEmotionResponseDTO;
import com.voiceprint.backend.domain.diary.Diary;
import com.voiceprint.backend.domain.diary.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmotionService {

    private final DiaryRepository diaryRepository;

    public WeeklyEmotionResponseDTO getWeeklyEmotions(Long userId) {

        // 1. 이번주 시작(일요일)과 끝(토요일) 계산
        LocalDate today = LocalDate.now(); // 오늘 날짜

        LocalDate startDate;
        // 1-1. 오늘이 일요일인 경우
        if (today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.debug("today is SUNDAY");
            startDate = today;
        // 1-2. 오늘이 일요일이 아닌 경우
        } else {
            log.debug("today is not SUNDAY");
            startDate = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        }

        LocalDate endDate = startDate.plusDays(6);

        log.debug("today : {}",today);
        log.debug("startDate : {}",startDate.atStartOfDay());
        log.debug("endDate : {}",endDate.atTime(LocalTime.MAX));

        // 2. 감정 정보 가져오기
        List<String> emotionList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            emotionList.add(null);
        }

        // 3. 감정 배열 생성
        List<Diary> diaries = diaryRepository.findByUserIdAndCreatedAtBetween(userId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        log.info("diaries : {}",diaries);

        for (Diary diary : diaries) {
            if (diary.getEmotion() == null) continue;

            int index = diary.getCreatedAt().getDayOfWeek().getValue() % 7 ;
            emotionList.set(index, diary.getEmotion().getName());
        }

        return new WeeklyEmotionResponseDTO(emotionList);
    }

    public MonthlyEmotionResponseDTO getMonthlyEmotions(Long userId) {

        // 1. 이번달 시작과 끝 계산
        LocalDate today = LocalDate.now(); // 오늘 날짜
        log.debug("today : {}",today);
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());
        log.debug("date range : {} ~ {}",startDate,endDate);

        // 2. 일기 목록 조회
        List<Diary> diaries = diaryRepository.findByUserIdAndCreatedAtBetween(
                userId, startDate.atStartOfDay(),endDate.atTime(LocalTime.MAX)
        );
        log.debug("diaries : {} ",diaries);

        //행복 설렘 피로 짜증 우울
        // 3. 감정 리스트 정의
        List<String> emotionList = List.of("행복","설렘","피로","짜증","우울");

        // 3. 감정 통계 조회
        Map<String, Integer> emotionMap = new HashMap<>();
        for (Diary diary : diaries) {
            log.debug("id:{}, emotion:{}",diary.getId(),diary.getEmotion().getName());
            if (diary.getEmotion() == null) continue;

            String name = diary.getEmotion().getName();
            emotionMap.put(name, emotionMap.getOrDefault(name,0)+1);
        }

        List<EmotionCountDTO> result = new ArrayList<>();
        for (String emo : emotionList) {
            result.add(new EmotionCountDTO(emo,emotionMap.getOrDefault(emo,0)));

        }

        return new MonthlyEmotionResponseDTO(result);
    }
}
