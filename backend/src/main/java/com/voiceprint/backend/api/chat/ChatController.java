package com.voiceprint.backend.api.chat;

import com.voiceprint.backend.api.chat.dto.*;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.chat.ChatServcie;
import com.voiceprint.backend.service.chat.ChatSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatServcie chatServcie;
    private final ChatSessionService chatSessionService;

    /**
     * Text 채팅 API
     */
    @PostMapping("/text")
    public ResponseEntity<CommonResponse<?>> chat(
            @RequestBody @Valid ChatTextRequestDTO request,
            HttpServletRequest httprequest
            ){
        Long userId = 1L; //Todo: 토큰 추출로 바꾸기
        ChatTextResponseDTO response = chatServcie.processChat(userId, request.getMessage());

        return ResponseEntity.ok(new CommonResponse<>(
                200, "성공", response
        ));

    }

    /**
     * 대화 종료 API
     */
    @PostMapping("/end")
    public ResponseEntity<CommonResponse<String>> endChatSession(
            HttpServletRequest request) {
        Long userId = 1L;
        // 비동기 처리
        chatSessionService.endSession(userId);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "세션 종료 성공", "일기 생성을 시작했습니다. (생성중입니다)"));
    }

    @GetMapping("/diary/temp")
    public ResponseEntity<CommonResponse<TempDiaryResponseDTO>> getTempDiary(
            HttpServletRequest request
    ) {
        Long userId = 1L;
        TempDiaryResponseDTO response = chatSessionService.getTempDiary(userId);
        return ResponseEntity.ok(new CommonResponse<>(200, "임시 일기 조회",response));
    }

    @PostMapping("/diary/temp/retry")
    public ResponseEntity<CommonResponse<String>> retryTempDiary(
            HttpServletRequest request
    ) {
        Long userId = 1L;

        chatSessionService.retryTempDiaryGeneration(userId);

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
            HttpServletRequest httprequest
    ) {

        Long userId = 1L;
        UpdateDiaryResult result = chatSessionService.updateTempDiary(userId, request);

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
}
