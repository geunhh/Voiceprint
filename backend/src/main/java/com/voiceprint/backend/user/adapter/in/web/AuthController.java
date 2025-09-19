package com.voiceprint.backend.user.adapter.in.web;

import com.voiceprint.backend.user.adapter.in.web.dto.*;
import com.voiceprint.backend.user.adapter.in.web.dto.ProfileResponse;
import com.voiceprint.backend.user.adapter.in.web.dto.TokenResponse;
import com.voiceprint.backend.user.adapter.in.web.dto.UserResponse;
import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.user.application.service.UserService;
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
    private final UserService authService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @PatchMapping("/profile")
    public ResponseEntity<CommonResponse<ProfileUpdateResponse>> updateProfile(
            HttpServletRequest request,
            @RequestBody ProfileUpdateRequest profileUpdateRequest) {

        Integer userId = authService.getUserIdFromRequest(request);
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
        Integer userId = authService.getUserIdFromRequest(request);
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

    /**
     * Google 로그인 페이지로 리다이렉트하는 엔드포인트
     *
     * @param response HTTP 응답 객체
     * @throws IOException 리다이렉트 중 발생할 수 있는 입출력 예외
     */
    @GetMapping("/{type}")
    public void redirectToGoogleLogin(
            @PathVariable String type, HttpServletResponse response) throws IOException {

        if (type.equals("google")) {
            response.sendRedirect("/oauth2/authorization/google");
        } else if (type.equals("kakao")) {
            response.sendRedirect("/oauth2/authorization/kakao");
        }
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
        Integer userId = authService.getUserIdFromAuthHeader(authHeader);

        // 사용자 ID가 없는 경우 (유효하지 않은 토큰이거나 사용자가 없는 경우)
        if (userId == null) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰이거나 사용자를 찾을 수 없습니다.");
        }
        UserResponse response = new UserResponse(userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 알림 수신 상태를 조회하는 API
     */
    @GetMapping("/reminder-setting")
    public ResponseEntity<CommonResponse<AlarmSettingsResponseDTO>> isReminderEnabled(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
//        Long userId = 1L;
        AlarmSettingsResponseDTO response = authService.isReminderEnabled(userId);

        return ResponseEntity.ok(
                new CommonResponse<>(200, "유저 알람 여부 조회 성공", response));
    }

    /**
     * 알림 조회 상태를 수정하는 API
     */
    @PatchMapping("/reminder-setting")
    public ResponseEntity<CommonResponse<Boolean>> updateReminderSetting(
            HttpServletRequest httprequest,
            @RequestBody @Valid ReminderSettingRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(httprequest);

       Boolean response = authService.updateReminderSetting(request.getEnableAlarms(), userId);

       return ResponseEntity.ok(new CommonResponse<>(
               200, "알림 여부가 설정되었습니다.",  response
       ));
    }

    /**
     * 알림 수신 시간을 설정하는 API
     */
    @PatchMapping("/reminder-time")
    public ResponseEntity<CommonResponse<String>> updateReminderTime(
            HttpServletRequest httprequest,
            @RequestBody @Valid ReminderTimeRequestDTO request
    ) {
        Integer userId = authService.getUserIdFromRequest(httprequest);

        String response = authService.updateReminderTime(request.getAlarmTime(), userId);

        if (response==null) {
            return ResponseEntity.ok(new CommonResponse<>(
                    400, "알림 시간의 형식이 누락되거나 잘못되었습니다.", null
            ));
        }
        return ResponseEntity.ok(new CommonResponse<>(
                200, "알림 시간이 설정되었습니다.", response
        ));
    }
}