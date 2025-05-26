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

    /**
     * 주간 감정 통계를 조회합니다.
     *
     * @param request HttpServletRequest (인증 토큰 포함)
     * @return 이번 주 감정 통계 응답
     */
    @GetMapping("/weekly")
    public ResponseEntity<CommonResponse<WeeklyEmotionResponseDTO>> getWeeklyEmotion(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        log.debug("이번주 사용자의 감정 정보 조회// userId : {}",userId);

        WeeklyEmotionResponseDTO response = emotionService.getWeeklyEmotions(userId);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "이번주 감정 데이터 조회 성공", response
        ));
    }

    /**
     * 월간 감정 통계를 조회합니다.
     *
     * @param request HttpServletRequest (인증 토큰 포함)
     * @return 이번 달 감정 통계 응답
     */
    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<MonthlyEmotionResponseDTO>> getMonthlyEmotion(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        log.info("이번달 사용자의 감정 통계 정보 조회// userId : {}",userId);

        MonthlyEmotionResponseDTO response = emotionService.getMonthlyEmotions(userId);

        return ResponseEntity.ok(new CommonResponse<>(
                200,"이번달 감정 통계 데이터 조회 성공", response
        )) ;

    }
}
