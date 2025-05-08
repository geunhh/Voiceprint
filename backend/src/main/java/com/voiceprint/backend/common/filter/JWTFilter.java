package com.voiceprint.backend.common.filter;

import com.voiceprint.backend.api.auth.dto.CustomOAuth2User;
import com.voiceprint.backend.common.util.JWTUtil;
import com.voiceprint.backend.domain.auth.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.getEmail(token); // 클레임에서 이메일 추출
        if (email == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증 객체 생성 및 설정
        CustomOAuth2User customUser = new CustomOAuth2User(
                User.builder()
                        .email(email)
                        .authProvider(User.AuthProvider.google) // 기본값. 실제 프로젝트에선 userService로 사용자 조회 필요
                        .build()
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities())
        );

        filterChain.doFilter(request, response);
    }
}
