package com.voiceprint.backend.api.chat.voice;

import com.voiceprint.backend.api.chat.dto.SessionStartRequestDTO;
import com.voiceprint.backend.api.chat.voice.dto.VoiceSessionResponseDto;
//import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.common.util.JWTUtil;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.chat.ChatSessionService;
import com.voiceprint.backend.service.chat.voice.VoiceChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/voice")
@RequiredArgsConstructor
public class VoiceController {

    private final AuthService authService;
    private final JWTUtil jwtUtil;
    private final VoiceChatService voiceChatService;
    /**
     * 음성 대화 세션 정보를 반환합니다.
     * 웹소켓 연결에 필요한 토큰과 URL을 제공합니다.
     */
    @GetMapping("/session")
    public ResponseEntity<VoiceSessionResponseDto> getVoiceSession(HttpServletRequest HttpRequest,
                                                                   @RequestParam("chatbotId") Byte chatbotId) {
        String token = jwtUtil.extractTokenFromHeader(HttpRequest.getHeader("Authorization"));
        log.info("챗봇 id!!!= "+chatbotId);

        if (token == null) {
            throw new UserNotFoundException("토큰이 없습니다.");
        }

        Integer userId = authService.getUserIdFromRequest(HttpRequest);

        if (userId == null) {
            throw new  UserNotFoundException("해당 유저가 없습니다.");
        }
        if (chatbotId == null) {
            throw new  UserNotFoundException("설정한 챗봇이 없습니다.");
        }

        // 웹소켓 연결 정보 생성
        VoiceSessionResponseDto responseDto = VoiceSessionResponseDto.builder()
                .wsUrl("ws://localhost:8080/ws?token=" + token)
                .userId(userId)
                .build();
        System.out.printf("######wsUrl= "+"ws://localhost:8080/ws?token=" + token);
        voiceChatService.startSession(userId, chatbotId);
        return ResponseEntity.ok(responseDto);
    }
}