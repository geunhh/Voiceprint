package com.voiceprint.backend.api.chat;

import com.voiceprint.backend.api.chat.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.api.chat.dto.SessionStartRequestDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.domain.chat.ChatSessionStatus;
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

    /**
     * 채팅 세션을 시작하는 API
     */
    @PostMapping("/start")
    public ResponseEntity<CommonResponse<?>> startSession(
        @Valid @RequestBody SessionStartRequestDTO request,
        HttpServletRequest httprequest // 유저 토큰
    ) {
        chatSessionService.startSession(1L, request.getChatbotId()); //Todo : 토큰으로 변경

        return ResponseEntity.ok(new CommonResponse<>(
                200,"대화 세션 생성 성공", null
        ));
    }



}
