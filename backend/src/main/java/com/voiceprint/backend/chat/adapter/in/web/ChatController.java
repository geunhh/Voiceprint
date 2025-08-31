package com.voiceprint.backend.chat.adapter.in.web;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatTextRequestDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatTextResponseDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.TempDiaryResponseDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.TempDiaryUpdateRequestDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.UpdateDiaryResult;
import com.voiceprint.backend.chat.application.port.in.ChatUseCase;
import com.voiceprint.backend.chat.application.port.in.GenerateDiaryUseCase;
import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.user.application.port.in.GetUserUseCase;
import com.voiceprint.backend.user.application.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final GenerateDiaryUseCase generateDiaryUseCase;
    private final GetUserUseCase authService;

    /**
     * Text 채팅 API
     */
    @PostMapping("/text")
    public ResponseEntity<CommonResponse<?>> chat(
            @RequestBody @Valid ChatTextRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        Integer userId = authService.getUserIdFromRequest(httpRequest);
        log.info("## 채팅 / userid : {}", userId);
        ChatTextResponseDTO response = chatUseCase.processChat(userId, request.getMessage());
        log.info("답변 : {}", response.getResponse());

        return ResponseEntity.ok(new CommonResponse<>(
                200, "성공", response
        ));
    }

    /**
     * 대화 종료 API
     */
    @PostMapping("/end")
    public ResponseEntity<CommonResponse<String>> endChatSession(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        log.info("## 채팅 세션 종료 / userid : {}", userId);
        generateDiaryUseCase.endChatSessionAndGenerateDiary(userId);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "세션 종료 성공", "일기 생성을 시작했습니다. (생성중입니다)"));
    }

    /**
     * 임시 일기 조회 API
     */
    @GetMapping("/diary/temp")
    public ResponseEntity<CommonResponse<TempDiaryResponseDTO>> getTempDiary(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        log.info("## 임시 일기 조회 / userid : {}", userId);
        TempDiaryResponseDTO response = generateDiaryUseCase.getTemporaryDiary(userId);
        log.info("## 일기 : {} ", response.getDiary());
        return ResponseEntity.ok(new CommonResponse<>(200, "임시 일기 조회", response));
    }

    @PostMapping("/diary/temp/retry")
    public ResponseEntity<CommonResponse<String>> retryTempDiary(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        log.info("## 임시 일기 재생성 / userid : {}", userId);
        generateDiaryUseCase.retryDiaryGeneration(userId);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new CommonResponse<>(
                        200, "임시 일기 재생성이 시작되었습니다.", null
                ));
    }

    /**
     * 임시 다이어리 수정 API
     */
    @PatchMapping("/diary/temp/update")
    public ResponseEntity<CommonResponse<TempDiaryResponseDTO>> updateTempDiary(
            @RequestBody @Valid TempDiaryUpdateRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        Integer userId = authService.getUserIdFromRequest(httpRequest);
        log.info("## 임시 채팅 수정 / userid : {}", userId);
        UpdateDiaryResult result = generateDiaryUseCase.updateTemporaryDiary(userId, request);

        if (!result.isChanged()) {
            return ResponseEntity.ok(new CommonResponse<>(
                    200, "변경된 내용이 없습니다.", result.getDiary()
            ));
        } else {
            return ResponseEntity.ok(new CommonResponse<>(
                    200, "임시 일기 수정 성공", result.getDiary()
            ));
        }
    }

    @PostMapping("/diary/temp/confirm")
    public ResponseEntity<CommonResponse<Map<String, Integer>>> confirmDiary(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        log.info("## 일기 확정!! / userid : {}", userId);
        Integer diaryId = generateDiaryUseCase.confirmDiary(userId);

        Map<String, Integer> data = Map.of("diaryId", diaryId);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "일기 저장 완료", data
        ));
    }
}
