package com.voiceprint.backend.chat.adapter.in.web;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatbotListResponseDTO;
import com.voiceprint.backend.chat.application.port.in.ChatbotUseCase;
import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
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
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotUseCase chatbotService;
    private final AuthService authService;

    /**
     * 챗봇 조회 API
     */
    @GetMapping
    public ResponseEntity<CommonResponse<ChatbotListResponseDTO>> getChatbots(HttpServletRequest request) {
        log.info("챗봇 조회 API 호출");
        Integer userId = authService.getUserIdFromRequest(request);

        ChatbotListResponseDTO response = chatbotService.getChatbots(userId);
        return ResponseEntity.ok(new CommonResponse<>(
                200,
                "챗봇 목록 조회 성공",
                response
        ));
    }
}
