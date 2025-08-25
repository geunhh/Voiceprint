package com.voiceprint.backend.chat.application.service.voice;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessage;
import com.voiceprint.backend.global.exception.chat.RedisUnavailableException;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.domain.Entity.ChatSessionStatus;
import com.voiceprint.backend.domain.Entity.Chatbot;
import com.voiceprint.backend.domain.Repository.ChatbotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VoiceChatService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatbotRepository chatbotRepository;
    private final UserRepository userRepository;
    @Value("${session.key}")
    private String sessionKeyPrefix;

    @Value("${message.key}")
    private String messageKeyPrefix;

    @Transactional(readOnly = true)
    public void startSession(Integer userId, Byte chatbotId) {
        String sessionKey = sessionKeyPrefix + ":" + userId;
        String messageKey = messageKeyPrefix + ":" + userId;

        // 1. Redis에 기존 세션이 있는지 확인
        Boolean hasKey = redisTemplate.hasKey(sessionKey);
        if (Boolean.TRUE.equals(hasKey)) {
            log.info("✅ 기존 세션이 존재하므로 초기화하지 않고 이어서 진행합니다. userId={}", userId);
            String status = (String) redisTemplate.opsForHash().get(sessionKey, "status");
            log.info("세션 상태= " + status);
            if ("DIARY_DONE".equals(status)) {
                // 새로 초기화하거나 종료
            }
            return;  // ❌ 초기화 없이 그대로 유지
        }

        try {
            // 2. DB에서 챗봇 프롬프트 조회
            Chatbot chatbot = chatbotRepository.findById(chatbotId)
                    .orElseThrow(() -> new IllegalArgumentException("챗봇 없음"));
            String prompt = chatbot.getPrompt();

            // 3. 사용자 정보 조회 및 최근 사용 챗봇 저장
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
            user.setLastChatbot(chatbot);
            userRepository.save(user);

            // 4. Redis에 세션 정보 저장
            Map<Object, Object> metadata = new HashMap<>();
            metadata.put("chatbotId", chatbotId);
            metadata.put("chatPrompt", prompt);
            metadata.put("status", ChatSessionStatus.IN_PROGRESS.name());
            metadata.put("total_token", 0);

            redisTemplate.opsForHash().putAll(sessionKey, metadata);

            // 5. 메시지 리스트가 없다면 초기 메시지 추가 (없으면 처음 접속이므로)
            Boolean messageExists = redisTemplate.hasKey(messageKey);
            if (Boolean.FALSE.equals(messageExists)) {
                String todayMessage = chatbot.getInitMent();
                redisTemplate.opsForList().rightPush(messageKey,
                        new ChatMessage("assistant", todayMessage));
            }

            log.info("🆕 세션 새로 생성 완료: userId={}, chatbotId={}", userId, chatbotId);

        } catch (RedisConnectionFailureException e) {
            log.error("❌ Redis 연결 실패", e);
            throw new RedisUnavailableException("Redis 서버 연결 실패");
        }
    }


    /**
     * Redis에 채팅 메시지를 저장합니다.
     */
    public void saveMessage(Integer userId, String role, String content) {
        String key = messageKeyPrefix + ":" + userId;
        ChatMessage message = new ChatMessage(role, content);
        redisTemplate.opsForList().rightPush(key, message);
        log.debug("📥 메시지 저장됨: {} - {}", role, content);
    }

    /**
     * 전체 토큰 수를 누적하여 저장합니다.
     */
    public void accumulateToken(Integer userId, Integer tokenDelta) {
        String sessionKey = sessionKeyPrefix + ":" + userId;

        Object value = redisTemplate.opsForHash().get(sessionKey, "total_token");
        Integer currentToken = (value instanceof Integer) ? (Integer) value : 0;

        int updated = currentToken + tokenDelta;
        redisTemplate.opsForHash().put(sessionKey, "total_token", updated);
        log.debug("🧮 토큰 누적: {} → {}", currentToken, updated);
    }
}
