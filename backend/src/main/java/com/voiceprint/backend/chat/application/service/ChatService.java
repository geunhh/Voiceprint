package com.voiceprint.backend.chat.application.service;

import com.voiceprint.backend.ai.domain.AiResult;
import com.voiceprint.backend.ai.domain.AiServicePort;
import com.voiceprint.backend.ai.domain.PromptFactory;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatTextResponseDTO;
import com.voiceprint.backend.chat.application.port.in.ChatUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ChatService implements ChatUseCase {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AiServicePort aiService;
    private final PromptFactory promptFactory;

    public ChatService(RedisTemplate<String, Object> redisTemplate, AiServicePort aiService, @Qualifier("chatPromptFactory") PromptFactory promptFactory) {
        this.redisTemplate = redisTemplate;
        this.aiService = aiService;
        this.promptFactory = promptFactory;
    }

    private static final int LIMIT_TOKEN = 2000;

    @Value("${session.key}")
    private String session_key;

    @Value("${message.key}")
    private String message_key;

    /**
     * Spring AI 활용
     */
    @Override
    @Transactional
    public ChatTextResponseDTO processChat(Integer userId, String message) {
        log.info("SpingAI 챗봇 호출 : {}", message);

        String uid = String.valueOf(userId);

        // 1) Prompt 생성
        Prompt prompt = promptFactory.buildChatPrompt(uid, message);
        log.info("## prompt : {}",prompt);

        // 2) 호출
        AiResult result = aiService.chat(prompt);

        String botResponse = result.getContent();
        log.info("## botResp : {} ",botResponse);
        int totalTokenDelta = message.length() + botResponse.length();

        // 3) Redis 업데이트 (누적 토큰/히스토리)
        String sessionKey = session_key + ":"+uid;
        String messageKey = message_key + ":"+uid;

        // 누적 토큰 저장
        Object cur = redisTemplate.opsForHash().get(sessionKey,"total_token");
        int newTotal = (cur == null ? 0 : Integer.parseInt(String.valueOf(cur))) + totalTokenDelta;
        redisTemplate.opsForHash().put(sessionKey, "total_token", newTotal);

        // 대화 히스토리 저장
        redisTemplate.opsForList().rightPush(messageKey, toJson("user", message));
        redisTemplate.opsForList().rightPush(messageKey, toJson("assistant", botResponse));

        int usageRate = (int) Math.round((double) newTotal / LIMIT_TOKEN * 100);
        return new ChatTextResponseDTO(botResponse, usageRate, newTotal);
    }

    private String toJson(String role, String content) {
        return "{\"role\":\"" + role + "\",\"content\":\"" + content.replace("\"","\\\"") + "\"}";
    }
}
