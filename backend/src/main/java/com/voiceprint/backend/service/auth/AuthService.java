package com.voiceprint.backend.service.auth;

import com.voiceprint.backend.common.util.JWTUtil;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Transactional(readOnly = true)
    public Long getUserIdFromRequest(HttpServletRequest request){
        String token = jwtUtil.extractTokenFromHeader(request.getHeader("Authorization"));

        return getUserIdFromAuthHeader(token);
    }

    /**
     * Authorization 헤더에서 토큰을 추출하고, 토큰 유효성을 검증한 후 사용자 ID를 반환합니다.
     *
     * @param authorizationHeader Bearer 토큰이 포함된 Authorization 헤더 값
     * @return 유효한 토큰이고 해당 이메일의 사용자가 존재하면 사용자 ID 반환, 그렇지 않으면 null 반환
     */
    @Transactional(readOnly = true)
    public Long getUserIdFromAuthHeader(String authorizationHeader) {
        // 헤더가 null이거나 Bearer로 시작하지 않으면 null 반환
        String token = jwtUtil.extractTokenFromHeader(authorizationHeader);

        // 토큰 유효성 검증
        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        // 토큰에서 이메일 추출
        String email = jwtUtil.getEmail(token);
        if (email == null) {
            return null;
        }

        // 이메일로 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 사용자가 존재하면 ID 반환, 없으면 null 반환
        return userOptional.map(User::getId).orElse(null);
    }

    /**
     * 토큰에서 이메일을 추출하고 해당 이메일의 사용자 ID를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 유효한 토큰이고 해당 이메일의 사용자가 존재하면 사용자 ID 반환, 그렇지 않으면 null 반환
     */
    @Transactional(readOnly = true)
    public Long getUserIdFromToken(String token) {
        // 토큰이 null이면 null 반환
        if (token == null) {
            return null;
        }

        try {
            // 토큰 유효성 검증
            if (!jwtUtil.validateToken(token)) {
                return null;
            }

            // 토큰에서 이메일 추출
            String email = jwtUtil.getEmail(token);
            if (email == null) {
                return null;
            }

            // 이메일로 사용자 조회
            Optional<User> userOptional = userRepository.findByEmail(email);

            // 사용자가 존재하면 ID 반환, 없으면 null 반환
            return userOptional.map(User::getId).orElse(null);
        } catch (Exception e) {
            // 토큰 처리 중 예외 발생 시 null 반환
            return null;
        }
    }

    /**
     * 이메일로 사용자 ID를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 해당 이메일의 사용자가 존재하면 사용자 ID 반환, 없으면 null 반환
     */
    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        // 이메일이 null이면 null 반환
        if (email == null) {
            return null;
        }

        // 이메일로 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 사용자가 존재하면 ID 반환, 없으면 null 반환
        return userOptional.map(User::getId).orElse(null);
    }
}