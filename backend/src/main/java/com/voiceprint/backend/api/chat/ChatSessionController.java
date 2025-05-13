package com.voiceprint.backend.api.chat;

import com.voiceprint.backend.api.chat.dto.ChatMessageListWithTokenDTO;
import com.voiceprint.backend.api.chat.dto.SessionStartRequestDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.domain.chat.ChatSessionStatus;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.chat.ChatSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat/session")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final AuthService authService;

    /**
     * 채팅 세션을 시작하는 API
     */
    @PostMapping("/start")
    public ResponseEntity<CommonResponse<?>> startSession(
        @Valid @RequestBody SessionStartRequestDTO request,
        HttpServletRequest httprequest // 유저 토큰
    ) {
        Long userId = authService.getUserIdFromRequest(httprequest);
        log.info("## 채팅 세션 시작 / userid : {}",userId);


        chatSessionService.startSession(userId, request.getChatbotId()); //Todo : 토큰으로 변경

        return ResponseEntity.ok(new CommonResponse<>(
                200,"대화 세션 생성 성공", null
        ));
    }

    /**
     * 진행 중인 채팅 세션을 확인하는 API
      */
    @GetMapping("/status")
    public ResponseEntity<CommonResponse<String>> getSessionStatus(
            HttpServletRequest request) {
        Long userId = authService.getUserIdFromRequest(request);
        log.info("## 채팅 세션 확인 / userid : {}",userId);
        ChatSessionStatus status = chatSessionService.getSessionStatus(userId);

        String statusName = (status != null) ? status.name() : null;
        log.info("status : {}",status);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "세션 상태 조회 성공", statusName
        ));
    }

    /**
     * 진행 중인 대화 메시지 전체 조회
     */
    @GetMapping("/messages")
    public ResponseEntity<CommonResponse<ChatMessageListWithTokenDTO>> getMessages(
            HttpServletRequest request) {
        Long userId = authService.getUserIdFromRequest(request);
        log.info("## 채팅 기록 조회 / userid : {}",userId);
        ChatMessageListWithTokenDTO response = chatSessionService.getMessages(userId);
        log.info("response : {}",response);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "메시지 조회 성공", response
        ));

    }

}
