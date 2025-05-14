package com.voiceprint.backend.api.diary;

import com.voiceprint.backend.api.diary.dto.MonthlyEmotionResponseDTO;
import com.voiceprint.backend.api.diary.dto.WeeklyEmotionResponseDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.diary.EmotionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/emotions")
public class EmotionController {

    private final EmotionService emotionService;
    private final AuthService authService;

    @GetMapping("/weekly")
    public ResponseEntity<CommonResponse<WeeklyEmotionResponseDTO>> getWeeklyEmotion(
            HttpServletRequest request
    ) {
        Long userId = authService.getUserIdFromRequest(request);
        log.info("이번주 사용자의 감정 정보 조회// userId : {}",userId);

        WeeklyEmotionResponseDTO response = emotionService.getWeeklyEmotions(userId);
        log.debug("조회 성공 // 감정: {}",response.getEmotions());
        return ResponseEntity.ok(new CommonResponse<>(
                200, "이번주 감정 데이터 조회 성공", response
        ));
    }

    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<MonthlyEmotionResponseDTO>> getMonthlyEmotion(
            HttpServletRequest request
    ) {
        Long userId = authService.getUserIdFromRequest(request);
//        Long userId = 2L;
        log.info("이번달 사용자의 감정 통계 정보 조회// userId : {}",userId);

        MonthlyEmotionResponseDTO response = emotionService.getMonthlyEmotions(userId);

        return ResponseEntity.ok(new CommonResponse<>(
                200,"이번달 감정 통계 데이터 조회 성공", response
        )) ;

    }
}
