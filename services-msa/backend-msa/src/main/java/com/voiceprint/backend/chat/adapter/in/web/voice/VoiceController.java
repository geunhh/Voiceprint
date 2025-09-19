package com.voiceprint.backend.chat.adapter.in.web.voice;

import com.voiceprint.backend.chat.adapter.in.web.voice.dto.VoiceSessionResponseDto;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.user.application.port.in.GetUserUseCase;
import com.voiceprint.backend.user.application.service.JWTUtil;
import com.voiceprint.backend.user.application.service.UserService;
import com.voiceprint.backend.chat.application.service.voice.VoiceChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/voice")
@RequiredArgsConstructor
public class VoiceController {

    private final GetUserUseCase authService;
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
                .wsUrl(websocket_url + token)
                .userId(userId)
                .build();

        System.out.printf("웹소켓 생성:"+ websocket_url + token);
        voiceChatService.startSession(userId,chatbotId);
        return ResponseEntity.ok(responseDto);
    }
}