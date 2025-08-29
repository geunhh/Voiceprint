package com.voiceprint.backend.chat.adapter.in.web;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageListWithTokenDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.SessionStartRequestDTO;
import com.voiceprint.backend.chat.application.port.in.ChatSessionUseCase;
import com.voiceprint.backend.chat.domain.ChatSessionStatus;
import com.voiceprint.backend.global.dto.CommonResponse;
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
@RequestMapping("/api/chat/session")
public class ChatSessionController {

    private final ChatSessionUseCase chatSessionUseCase;
    private final UserService authService;

    /**
     * 채팅 세션을 시작하는 API
     */
    @PostMapping("/start")
    public ResponseEntity<CommonResponse<?>> startSession(
            @Valid @RequestBody SessionStartRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        Integer userId = authService.getUserIdFromRequest(httpRequest);
        log.info("## 채팅 세션 시작 / userid : {}", userId);

        chatSessionUseCase.startChatSession(userId, request.getChatbotId());

        return ResponseEntity.ok(new CommonResponse<>(
                200, "대화 세션 생성 성공", null
        ));
    }

    /**
     * 진행 중인 채팅 세션을 확인하는 API
      */
    @GetMapping("/status")
    public ResponseEntity<CommonResponse<String>> getSessionStatus(
            HttpServletRequest httpRequest
    ) {
        Integer userId = authService.getUserIdFromRequest(httpRequest);
        log.info("## 채팅 세션 확인 / userid : {}", userId);
        ChatSessionStatus status = chatSessionUseCase.getChatSessionStatus(userId);

        String statusName = (status != null) ? status.name() : null;
        log.info("status : {}", status);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "세션 상태 조회 성공", statusName
        ));
    }

    /**
     * 진행 중인 대화 메시지 전체 조회
     */
    @GetMapping("/messages")
    public ResponseEntity<CommonResponse<ChatMessageListWithTokenDTO>> getMessages(
            HttpServletRequest httpRequest
    ) {
        Integer userId = authService.getUserIdFromRequest(httpRequest);
        log.info("## 채팅 기록 조회 / userid : {}", userId);
        ChatMessageListWithTokenDTO response = chatSessionUseCase.getChatHistory(userId);
        log.info("response : {}", response);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "메시지 조회 성공", response
        ));
    }
}
