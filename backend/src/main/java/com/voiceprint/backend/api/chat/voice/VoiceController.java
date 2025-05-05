package com.voiceprint.backend.api.chat.voice;

import com.voiceprint.backend.api.chat.voice.dto.VoiceSessionResponseDto;
//import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/voice")
@RequiredArgsConstructor
public class VoiceController {

//    private final AuthService authService;

    /**
     * 음성 대화 세션 정보를 반환합니다.
     * 웹소켓 연결에 필요한 토큰과 URL을 제공합니다.
     */
    @GetMapping("/session")
    public ResponseEntity<VoiceSessionResponseDto> getVoiceSession(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(null);
        }

        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        if (userId == null) {
            return ResponseEntity.status(401).body(null);
        }

        // 웹소켓 연결 정보 생성
        VoiceSessionResponseDto responseDto = VoiceSessionResponseDto.builder()
                .wsUrl("ws://localhost:8080/ws?token=" + authHeader.substring(7))
                .userId(userId)
                .build();

        return ResponseEntity.ok(responseDto);
    }
}