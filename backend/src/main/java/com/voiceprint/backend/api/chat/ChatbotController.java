package com.voiceprint.backend.api.chat;

import com.voiceprint.backend.api.chat.dto.ChatbotResponseDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.domain.chat.Chatbot;
import com.voiceprint.backend.service.chat.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * 챗봇 조회 API
     */
    @GetMapping
    public ResponseEntity<CommonResponse<List<ChatbotResponseDTO>>> getChatbots(HttpServletRequest request) {
        List<ChatbotResponseDTO> chatbots = chatbotService.getChatbots();
        return ResponseEntity.ok(new CommonResponse<>(
                200,
                "챗봇 목록 조회 성공",
                chatbots
        ));
    }
}
