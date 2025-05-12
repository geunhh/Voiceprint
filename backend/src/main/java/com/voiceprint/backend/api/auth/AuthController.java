package com.voiceprint.backend.api.auth;

import com.voiceprint.backend.api.auth.dto.*;
import com.voiceprint.backend.api.auth.dto.ProfileResponse;
import com.voiceprint.backend.api.auth.dto.TokenResponse;
import com.voiceprint.backend.api.auth.dto.UserResponse;
import com.voiceprint.backend.common.config.OAuth2SuccessHandler;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @PatchMapping("/profile")
    public ResponseEntity<CommonResponse<ProfileUpdateResponse>> updateProfile(
            HttpServletRequest request,
            @RequestBody ProfileUpdateRequest profileUpdateRequest) {

        Long userId = authService.getUserIdFromRequest(request);
        ProfileUpdateResponse updatedProfile = authService.updateProfile(userId, profileUpdateRequest);

        return ResponseEntity.ok(new CommonResponse<>(200, "프로필 수정 완료", updatedProfile));
    }


    @GetMapping("/profileimage")
    public ResponseEntity<CommonResponse<List<ProfileImageResponse>>> getProfileImage() {
        List<ProfileImageResponse> profileImages = authService.getProfileImages();
        return ResponseEntity.ok(new CommonResponse<>(200, "프로필 이미지 조회 완료", profileImages));
    }



    /**
     * 유저 프로필 조회
     * 최근 일기 리스트로 응답
     */
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<ProfileResponse>> getProfile(HttpServletRequest request) {
        Long userId = authService.getUserIdFromRequest(request);
        ProfileResponse response = authService.getProfile(userId);
        return ResponseEntity.ok(new CommonResponse<>(200,"프로필 조회 완료.", response));
    }

    /**
     * 액세스 토큰 재발급
     * 리프레시 토큰은 쿠키에서 추출
     */
    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<Map<String, Object>>> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonResponse<>(401, "리프레시 토큰이 없습니다. 다시 로그인해주세요.", null));
        }

        try {
            // 토큰 재발급 서비스 호출
            TokenResponse tokenResponse = authService.reissueToken(refreshToken);

            // 새 리프레시 토큰을 쿠키에 설정
            Cookie refreshTokenCookie = oAuth2SuccessHandler.createCookie("refreshToken", tokenResponse.getRefreshToken());
            response.addCookie(refreshTokenCookie);

            // 응답 헤더에 액세스 토큰 추가
            response.setHeader("Authorization", "Bearer " + tokenResponse.getAccessToken());

            // 응답 바디에는 액세스 토큰과 사용자 ID만 포함
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("accessToken", tokenResponse.getAccessToken());
            responseData.put("userId", tokenResponse.getUserId());

            return ResponseEntity.ok(new CommonResponse<>(200, "토큰이 성공적으로 재발급되었습니다.", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CommonResponse<>(401, e.getMessage(), null));
        }
    }

    /**
     * 로그아웃 처리
     * - 쿠키에서 리프레시 토큰 추출
     * - Redis에서 리프레시 토큰 삭제
     * - 쿠키 무효화
     */
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = extractRefreshTokenFromCookie(request);

        // 리프레시 토큰이 있으면 Redis에서 삭제
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // 쿠키 무효화
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new CommonResponse<>(200, "로그아웃 되었습니다.", null));
    }

    /**
     * 쿠키에서 리프레시 토큰 추출
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @GetMapping("/google")
    public void redirectToGoogleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");  // Spring Security OAuth2 URL로 리다이렉트
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

    @GetMapping("/reminder-setting")
    public ResponseEntity<CommonResponse<Boolean>> isReminderEnabled(
            HttpServletRequest request
    ) {
//        Long userId = authService.getUserIdFromRequest(request);
        Long userId = 2L;
        Boolean isEnabled = authService.isReminderEnabled(userId);

        return ResponseEntity.ok(
                new CommonResponse<>(200, "유저 알람 여부 조회 성공", isEnabled));
    }

    @PatchMapping("/reminder-setting")
    public ResponseEntity<CommonResponse<Boolean>> updateReminderSetting(
            HttpServletRequest httprequest,
            @RequestBody @Valid ReminderSettingRequest request
    ) {
       Long userId = authService.getUserIdFromRequest(httprequest);

       Boolean response = authService.updateReminderSetting(request.getEnableAlarms(), userId);

       return ResponseEntity.ok(new CommonResponse<>(
               200, "알림 여부가 설정되었습니다.",  response
       ));
    }
}