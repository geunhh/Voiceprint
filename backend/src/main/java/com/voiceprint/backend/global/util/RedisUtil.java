package com.voiceprint.backend.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveText(Integer userId, String transcript) {
        String sessionKey = "voice:session:" + userId;
        redisTemplate.opsForHash().put(sessionKey, "transcript", transcript);
    }

    public void saveSession(Integer userId, Map<String, Object> data) {
        String sessionKey = "voice:session:" + userId;
        redisTemplate.opsForHash().putAll(sessionKey, data);
    }

    public void updateStatus(Integer userId, String status) {
        String sessionKey = "voice:session:" + userId;
        redisTemplate.opsForHash().put(sessionKey, "status", status);
    }

    public void deleteSession(Integer userId) {
        String sessionKey = "voice:session:" + userId;
        redisTemplate.delete(sessionKey);
    }
}
