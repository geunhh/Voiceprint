package com.voiceprint.backend.api.thema;

import com.voiceprint.backend.api.thema.dto.DiaryThemaCreateRequest;
import com.voiceprint.backend.api.thema.dto.DiaryThemaCreateResponse;
import com.voiceprint.backend.api.thema.dto.DiaryThemaListResponseDTO;
import com.voiceprint.backend.api.thema.dto.UsingDiaryThemaResponseDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.domain.Repository.DiaryThemaRepository;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.thema.DiaryThemaService;
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

    private final DiaryThemaRepository diaryThemaRepository;
    private final DiaryThemaService diaryThemaService;
    private final AuthService authService;

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<DiaryThemaListResponseDTO>> getThmeas(
            HttpServletRequest request    ) {
//        Long userId = 1L;
        Long userId = authService.getUserIdFromRequest(request);
        log.info("## 일기 테마 전체 조회 / userid : {}",userId);
        DiaryThemaListResponseDTO response = diaryThemaService.getThemasForUser(userId);
        return ResponseEntity.ok(
                new CommonResponse<>(200,"일기 테마 조회 성공",response));
    }

    @PutMapping("/select/{themaId}")
    public ResponseEntity<CommonResponse<Void>> selectTheam(
            @PathVariable Long themaId,
            HttpServletRequest request) {
//        Long userId = 1L;

        Long userId = authService.getUserIdFromRequest(request);
        log.info("## 일기 테마 선택 / userid : {}",userId);
        diaryThemaService.selectThema(userId,themaId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "사용자 테마 설정 완료", null
        ));
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<DiaryThemaCreateResponse>> createThema(
            @Valid @RequestBody DiaryThemaCreateRequest request,
            HttpServletRequest httprequest
    ) {
//        Long userId = 1L;
        Long userId = authService.getUserIdFromRequest(httprequest);
        log.info("## 커스텀 테마 생성 / userid : {}",userId);

        DiaryThemaCreateResponse response = diaryThemaService.createCustomThema(userId, request.getExampleDiary());

        return ResponseEntity.ok(new CommonResponse<>(
                201, "커스템 테마 생성 완료", response
        ));
    }

    @PatchMapping("/extract/{diaryId}")
    public ResponseEntity<CommonResponse<?>> extractThema(
            @PathVariable Long diaryId,
            HttpServletRequest httprequest
    ) {
//        Long userId = 1L;
        Long userId = authService.getUserIdFromRequest(httprequest);
        log.info("## 일기에서 테마 추출 / userid : {}",userId);
        diaryThemaService.updateCustomThemaFromDiary(userId,diaryId);

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
        Long userId = authService.getUserIdFromRequest(request);
        UsingDiaryThemaResponseDTO response = diaryThemaService.getUsingThema(userId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "현재 사용 중인 테마 ID 조회 성공", response
        ));

    }

}
