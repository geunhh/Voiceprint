package com.voiceprint.backend.service.chat.voice;

import com.voiceprint.backend.api.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceChatService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${session.key}")
    private String sessionKeyPrefix;

    @Value("${message.key}")
    private String messageKeyPrefix;

    /**
     * Redis에 채팅 메시지를 저장합니다.
     */
    public void saveMessage(Long userId, String role, String content) {
        String key = messageKeyPrefix + ":" + userId;
        ChatMessage message = new ChatMessage(role, content);
        redisTemplate.opsForList().rightPush(key, message);
        log.debug("📥 메시지 저장됨: {} - {}", role, content);
    }

    /**
     * 전체 토큰 수를 누적하여 저장합니다.
     */
    public void accumulateToken(Long userId, int tokenDelta) {
        String sessionKey = sessionKeyPrefix + ":" + userId;

        Object value = redisTemplate.opsForHash().get(sessionKey, "total_token");
        int currentToken = (value instanceof Integer) ? (Integer) value : 0;

        int updated = currentToken + tokenDelta;
        redisTemplate.opsForHash().put(sessionKey, "total_token", updated);
        log.debug("🧮 토큰 누적: {} → {}", currentToken, updated);
    }
}
