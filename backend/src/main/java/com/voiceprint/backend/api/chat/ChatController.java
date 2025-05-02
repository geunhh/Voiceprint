package com.voiceprint.backend.api.chat;

import com.voiceprint.backend.api.chat.dto.ChatTextResponseDTO;
import com.voiceprint.backend.api.chat.dto.ChatTextRequestDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.chat.ChatServcie;
import com.voiceprint.backend.service.chat.ChatSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
