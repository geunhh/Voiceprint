package com.voiceprint.backend.service.diary;

import com.voiceprint.backend.api.diary.dto.EmotionCountDTO;
import com.voiceprint.backend.api.diary.dto.MonthlyEmotionResponseDTO;
import com.voiceprint.backend.api.diary.dto.WeeklyEmotionResponseDTO;
import com.voiceprint.backend.domain.Entity.Diary;
import com.voiceprint.backend.domain.Repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final List<String> EMOTIONS = List.of("행복", "설렘", "피로", "짜증", "우울");

    /**
     * 사용자의 이번 주 감정을 요일별로 조회합니다.
     */
    public WeeklyEmotionResponseDTO getWeeklyEmotions(Integer userId) {

        DateRange range = getCurrentWeekRange();
        log.debug("주간 범위: {} ~ {}", range.start(), range.end());

        // 2. 감정 정보 가져오기
        List<String> emotionList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            emotionList.add(null);
        }        List<Diary> diaries = diaryRepository.findByUserIdAndCreatedAtBetween(userId, range.start(), range.end());
        log.debug("diaries 조회 : {}",diaries);

        for (Diary diary : diaries) {
            if (diary.getEmotion() == null) continue;

            int index = diary.getCreatedAt().getDayOfWeek().getValue() % 7 ;
            emotionList.set(index, diary.getEmotion().getName());
        }

        return new WeeklyEmotionResponseDTO(emotionList);
    }

    /**
     * 사용자의 이번 달 감정을 통계로 조회합니다.
     */
    public MonthlyEmotionResponseDTO getMonthlyEmotions(Integer userId) {
        DateRange range = getCurrentMonthRange();
        log.debug("월간 범위: {} ~ {}", range.start(), range.end());

        // 2. 일기 목록 조회
        List<Diary> diaries = diaryRepository.findByUserIdAndCreatedAtBetween(
                userId, range.start(), range.end()
        );
        log.debug("diaries 조회 : {} ",diaries);

        // 3. 감정 통계 조회
        Map<String, Integer> emotionMap = new HashMap<>();
        for (Diary diary : diaries) {
            log.debug("id:{}, emotion:{}",diary.getId(),diary.getEmotion().getName());

            if (diary.getEmotion() == null) continue;
            String name = diary.getEmotion().getName();
            emotionMap.put(name, emotionMap.getOrDefault(name,0)+1);
        }

        List<EmotionCountDTO> result = EMOTIONS.stream()
                .map(emotion -> new EmotionCountDTO(emotion,emotionMap.getOrDefault(emotion,0)))
                .toList();

        return new MonthlyEmotionResponseDTO(result);
    }

    // ========== 날짜 계산 유틸 ========== //

    private DateRange getCurrentWeekRange() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.getDayOfWeek() == DayOfWeek.SUNDAY
                ? today
                : today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        LocalDate end = start.plusDays(6);
        return new DateRange(start.atStartOfDay(),end.atTime(LocalTime.MAX));
    }

    private DateRange getCurrentMonthRange() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
        return new DateRange(start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }

    /**
     * 날짜 범위를 나타내는 내부 레코드
     */
    private record DateRange(LocalDateTime start, LocalDateTime end){}
}
