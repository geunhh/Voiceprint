package com.voiceprint.backend.common.config;

import com.voiceprint.backend.api.auth.dto.CustomOAuth2User;
import com.voiceprint.backend.common.util.JWTUtil;
import com.voiceprint.backend.domain.auth.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.jwt.redirect.url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();

            String email = user.getUsername();
            Long userId = user.getUserId();
            String accessToken = jwtUtil.createAccessToken(email);
            String refreshToken = jwtUtil.createRefreshToken(user.getUserId());

            // Redis에 리프레시 토큰 저장
            refreshTokenRepository.saveRefreshToken(userId, refreshToken);

            response.setHeader("Authorization", "Bearer " + accessToken);

            // refresh 토큰은 쿠키, access는 url로 반환
            Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken);
            response.addCookie(refreshTokenCookie);

            // 프론트엔드로 리다이렉션
            response.sendRedirect(redirectUrl + "/login-success?access=" + accessToken);

        } catch (Exception e) {
            System.out.println("OAuth2 성공 핸들러 오류: " + e.getMessage());
            response.sendRedirect("/login?error");
        }
    }

    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60); // 24시간
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발 환경에서는 false, 운영에서는 true
        // SameSite 설정 (Spring Boot 버전에 따라 다름)
//        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}