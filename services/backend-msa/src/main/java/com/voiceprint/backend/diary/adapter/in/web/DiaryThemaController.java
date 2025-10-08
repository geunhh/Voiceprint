package com.voiceprint.backend.diary.adapter.in.web;

import com.voiceprint.backend.diary.adapter.in.web.dto.thema.DiaryThemaCreateRequest;
import com.voiceprint.backend.diary.adapter.in.web.dto.thema.DiaryThemaCreateResponse;
import com.voiceprint.backend.diary.adapter.in.web.dto.thema.DiaryThemaListResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.thema.UsingDiaryThemaResponseDTO;
import com.voiceprint.backend.diary.application.port.in.DiaryThemaUseCase;
import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.user.application.port.in.GetUserUseCase;
import com.voiceprint.backend.user.application.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/thema")
public class DiaryThemaController {

    private final DiaryThemaUseCase diaryThemaUsecase;
    private final GetUserUseCase authService;

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<DiaryThemaListResponseDTO>> getThemas(
            HttpServletRequest request    ) {

        Integer userId = authService.getUserIdFromRequest(request);
        log.info("## 일기 테마 전체 조회 / userid : {}",userId);
        DiaryThemaListResponseDTO response = diaryThemaUsecase.getThemasForUser(userId);
        return ResponseEntity.ok(
                new CommonResponse<>(200,"일기 테마 조회 성공",response));
    }

    @PutMapping("/select/{themaId}")
    public ResponseEntity<CommonResponse<Void>> selectThema(
            @PathVariable Integer themaId,
            HttpServletRequest request) {

        Integer userId = authService.getUserIdFromRequest(request);
        log.info("## 일기 테마 선택 / userid : {}",userId);
        diaryThemaUsecase.selectThema(userId,themaId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "사용자 테마 설정 완료", null
        ));
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<DiaryThemaCreateResponse>> createThema(
            @Valid @RequestBody DiaryThemaCreateRequest request,
            HttpServletRequest httprequest
    ) {
        Integer userId = authService.getUserIdFromRequest(httprequest);
        log.info("## 커스텀 테마 생성 / userid : {}",userId);

        DiaryThemaCreateResponse response = diaryThemaUsecase.createCustomThema(userId, request.getExampleDiary());

        return ResponseEntity.ok(new CommonResponse<>(
                201, "커스템 테마 생성 완료", response
        ));
    }

    @PatchMapping("/extract/{diaryId}")
    public ResponseEntity<CommonResponse<?>> extractThema(
            @PathVariable Integer diaryId,
            HttpServletRequest httprequest
    ) {
        Integer userId = authService.getUserIdFromRequest(httprequest);
        log.info("## 일기에서 테마 추출 / userid : {}",userId);
        diaryThemaUsecase.updateCustomThemaFromDiary(userId,diaryId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "커스텀 테마 수정 완료", null
        ));
    }

    /**
     * 유저 UsingThema 조회 API
     */
    @GetMapping("/using")
    public ResponseEntity<CommonResponse<UsingDiaryThemaResponseDTO>> getUsingThema(
            HttpServletRequest request
    ) {
        log.info("### UsingThema 조회 API 호출");
        Integer userId = authService.getUserIdFromRequest(request);
//        Integer userId = 1;
        UsingDiaryThemaResponseDTO response = diaryThemaUsecase.getUsingThema(userId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "현재 사용 중인 테마 ID 조회 성공", response
        ));

    }

}
