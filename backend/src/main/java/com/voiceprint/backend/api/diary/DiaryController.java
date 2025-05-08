package com.voiceprint.backend.api.diary;

import com.voiceprint.backend.api.diary.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.api.diary.dto.DiaryListWithCursorDTO;
import com.voiceprint.backend.api.diary.dto.DiaryMontlyListDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.domain.diary.DiaryRepository;
import com.voiceprint.backend.service.diary.DiaryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@RequestMapping("/diaries")
public class DiaryController {
    private final DiaryService diaryService;
    private final DiaryRepository diaryRepository;

    /**
     * diaryId를 기반으로 일기 상세정보를 조회하는 API
     */
    @GetMapping("/diary/{diaryId}")
    public ResponseEntity<CommonResponse<DiaryDetailResponseDTO>> getDiaryDetail(
            @PathVariable Long diaryId,
            HttpServletRequest request    ){
        log.info("다이어리 상세보기 호출 diaryId: {}",diaryId);

        DiaryDetailResponseDTO response = diaryService.getDiaryDetail(request, diaryId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "조회 성공", response
        ));
    }

    /**
     * 내가 작성한 일기를 조회하는 API
     * @param size
     * @param request
     */
    @GetMapping("/me/diaries")
    public ResponseEntity<CommonResponse<DiaryListWithCursorDTO>> getMyDiaries(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "7") int size,
            HttpServletRequest request
    ) {

        DiaryListWithCursorDTO result = diaryService.getUserDiaries(request, cursor, size);
        return ResponseEntity.ok(new CommonResponse<>(200, "성공", result));
    }

    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<DiaryMontlyListDTO>> getMonthlyDiaries(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request
    ) {

        DiaryMontlyListDTO response = diaryService.getMonthlyDiaries(request, year, month);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "조회 성공", response
        ));
    }

}
