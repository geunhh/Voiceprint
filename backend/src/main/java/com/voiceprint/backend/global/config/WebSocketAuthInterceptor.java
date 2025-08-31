package com.voiceprint.backend.global.config;

import com.voiceprint.backend.user.application.port.in.GetUserUseCase;
import com.voiceprint.backend.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final GetUserUseCase authService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            // ✅ 쿼리 파라미터에서 token 추출
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null) {
                Integer userId = authService.getUserIdFromToken(token);
                if (userId != null) {
                    attributes.put("userId", userId);
                    log.info("웹소켓 유저{} 인증 완료", userId);
                    return true;
                }
            }
            log.error("웹소켓 유저 인증 실패");
            // ❌ token 없거나 인증 실패
            return false;
        }
        log.error("요청 타입이 ServletServerHttpRequest 아닙니다.");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 이후 처리 (선택적)
    }
}