package com.voiceprint.backend.api.auth;

import com.voiceprint.backend.api.auth.dto.UserResponse;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.http.HttpResponse;
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Controller
public class AuthController {
    private final AuthService authService;

    @GetMapping("/google")
    public String redirectToGoogleLogin() {
        return "redirect:/oauth2/authorization/google";  // Spring Security OAuth2 URL로 리다이렉트
    }

    @GetMapping("/search")
    public ResponseEntity<?>findUser(HttpServletRequest request) {
        // Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader("Authorization");

        // 헤더가 없거나 형식이 잘못된 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("인증 토큰이 없거나 형식이 잘못되었습니다.");
        }
        // 토큰에서 사용자 ID 조회
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        // 사용자 ID가 없는 경우 (유효하지 않은 토큰이거나 사용자가 없는 경우)
        if (userId == null) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰이거나 사용자를 찾을 수 없습니다.");
        }
        UserResponse response = new UserResponse(userId);

        return ResponseEntity.ok(response);
    }
}