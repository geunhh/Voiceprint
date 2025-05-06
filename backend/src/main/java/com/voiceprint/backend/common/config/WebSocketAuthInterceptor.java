package com.voiceprint.backend.config;

//import com.voiceprint.backend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

//    private final AuthService authService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

            // 1. 헤더에서 토큰으로 인증
            String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                Long userId = 1L;
                // backend 병합 후 주석 해제
//                Long userId = authService.getUserIdFromAuthHeader(authHeader);
                if (userId != null) {
                    attributes.put("userId", userId);
                    return true;
                }
            }

            // 2. URL 쿼리 파라미터에서 토큰으로 인증 (WebSocket은 헤더 설정이 제한적일 수 있음)
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null) {
                Long userId = 1L;
//                Long userId = authService.getUserIdFromToken(token);
                if (userId != null) {
                    attributes.put("userId", userId);
                    return true;
                }
            }

            // 인증 실패
            return false;
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 이후 처리 (선택적)
    }
}