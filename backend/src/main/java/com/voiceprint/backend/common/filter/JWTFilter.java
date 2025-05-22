package com.voiceprint.backend.common.filter;

import com.voiceprint.backend.api.auth.dto.CustomOAuth2User;
import com.voiceprint.backend.common.util.JWTUtil;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
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
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 재발급 경로는 필터링하지 않음
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/api/v1/user/reissue")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtil.isExpired(token)) {
                setJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰이 만료되었습니다. 다시 로그인해주세요.");
                return;
            }

            String providerId = jwtUtil.getProviderId(token);
            if (providerId == null) {
                setJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
                return;
            }

            User user = userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));

            CustomOAuth2User customUser = new CustomOAuth2User(user);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities())
            );

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            setJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰이 만료되었습니다. 다시 로그인해주세요.");
        } catch (Exception e) {
            setJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "인증 중 오류가 발생했습니다.");
        }
    }

    private void setJsonResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }}

