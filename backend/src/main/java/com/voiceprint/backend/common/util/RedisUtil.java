package com.voiceprint.backend.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveText(Long userId, String transcript) {
        String sessionKey = "voice:session:" + userId;
        redisTemplate.opsForHash().put(sessionKey, "transcript", transcript);
    }

    public void saveSession(Long userId, Map<String, Object> data) {
        String sessionKey = "voice:session:" + userId;
        redisTemplate.opsForHash().putAll(sessionKey, data);
    }

    public void updateStatus(Long userId, String status) {
        String sessionKey = "voice:session:" + userId;
        redisTemplate.opsForHash().put(sessionKey, "status", status);
    }

    public void deleteSession(Long userId) {
        String sessionKey = "voice:session:" + userId;
        redisTemplate.delete(sessionKey);
    }
}
