package com.voiceprint.backend.domain.Repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final long REFRESH_TOKEN_TTL = 24 * 60 * 60; // 1일 (초 단위)

    /**
     * 리프레시 토큰을 Redis에 저장합니다.
     * 키는 "RT:{userId}" 형식으로 저장됩니다.
     */
    public void saveRefreshToken(Long userId, String refreshToken) {
        ValueOperations<String, Object> vop = redisTemplate.opsForValue();
        String key = getKey(userId);
        vop.set(key, refreshToken, REFRESH_TOKEN_TTL, TimeUnit.SECONDS);
    }

    /**
     * 사용자 ID로 저장된 리프레시 토큰을 조회합니다.
     */
    public String findRefreshToken(Long userId) {
        ValueOperations<String, Object> vop = redisTemplate.opsForValue();
        String key = getKey(userId);
        Object value = vop.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 사용자 ID로 저장된 리프레시 토큰을 삭제합니다.
     */
    public void deleteRefreshToken(Long userId) {
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

    /**
     * 리프레시 토큰의 키를 생성합니다.
     */
    private String getKey(Long userId) {
        return "RT:" + userId;
    }
}
