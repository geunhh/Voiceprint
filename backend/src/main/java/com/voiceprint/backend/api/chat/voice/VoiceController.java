package com.voiceprint.backend.api.chat.voice;

import com.voiceprint.backend.api.chat.dto.SessionStartRequestDTO;
import com.voiceprint.backend.api.chat.voice.dto.VoiceSessionResponseDto;
//import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.common.util.JWTUtil;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.chat.ChatSessionService;
import com.voiceprint.backend.service.chat.voice.VoiceChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/voice")
@RequiredArgsConstructor
public class VoiceController {

    private final AuthService authService;
    private final JWTUtil jwtUtil;
    private final VoiceChatService voiceChatService;
    @Value("${back-websocket.url}")
    private String websocket_url;
    /**
     * 음성 대화 세션 정보를 반환합니다.
     * 웹소켓 연결에 필요한 토큰과 URL을 제공합니다.
     */
    @GetMapping("/session")
    public ResponseEntity<VoiceSessionResponseDto> getVoiceSession(HttpServletRequest HttpRequest,
                                                                   @RequestParam("chatbotId") Byte chatbotId) {
        String token = jwtUtil.extractTokenFromHeader(HttpRequest.getHeader("Authorization"));


        if (token == null) {
            return ResponseEntity.status(401).body(null);
        }

        Integer userId = authService.getUserIdFromRequest(HttpRequest);

        if (userId == null) {
            return ResponseEntity.status(401).body(null);
        }

        // 웹소켓 연결 정보 생성
        VoiceSessionResponseDto responseDto = VoiceSessionResponseDto.builder()
                .wsUrl(websocket_url + token)
                .userId(userId)
                .build();
        System.out.printf("웹소켓 생성:"+ websocket_url + token);
        voiceChatService.startSession(userId, (chatbotId != null)? chatbotId : 1);
        return ResponseEntity.ok(responseDto);
    }
}