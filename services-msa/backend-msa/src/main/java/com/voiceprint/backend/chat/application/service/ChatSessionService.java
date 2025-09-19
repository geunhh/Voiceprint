package com.voiceprint.backend.chat.application.service;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageListWithTokenDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.chat.application.port.in.ChatSessionUseCase;
import com.voiceprint.backend.chat.application.port.out.ChatbotRepositoryPort;
import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;

import com.voiceprint.backend.user.domain.User;
import com.voiceprint.backend.chat.domain.Chatbot;
import com.voiceprint.backend.chat.domain.ChatMessage;
import com.voiceprint.backend.chat.domain.ChatSessionStatus;
import com.voiceprint.backend.global.exception.chat.RedisUnavailableException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatSessionService implements ChatSessionUseCase {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatbotRepositoryPort chatbotRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Value("${session.key}")
    private String session_key;

    @Value("${message.key}")
    private String message_key;

    /**
     * 세션을 시작하는 메소드
     */
    @Override
    @Transactional
    public void startChatSession(Integer userId, Byte chatbotId) {
        String sessionKey = session_key + ":" + userId;
        String messageKey = message_key + ":" + userId;

        // 1.Redis에 기존 세션 존재하는지 확인
        Boolean hasKey = redisTemplate.hasKey(sessionKey);
        if (Boolean.TRUE.equals(hasKey)) {
            log.debug("이미 진행 중인 세션이 있습니다.");
            // 기존 세션 삭제.
            redisTemplate.delete(sessionKey);
        }
        try {
            // 2.DB에 Session 레코드 생성
            // 사용자ID, 챗봇ID, 생성일시, 상태값 설정.

            // 3. 챗봇 프롬프트 조회
            Chatbot chatbot = chatbotRepositoryPort.findById(chatbotId)
                    .orElseThrow(() -> new IllegalArgumentException("챗봇 없음"));
            String prompt = chatbot.getPrompt();

            User user = userRepositoryPort.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
            // user.setLastChatbot(chatbot);
            userRepositoryPort.save(user);


            // 4. Redis 저장 : 챗봇ID, 챗봇 prompt, status
            Map<Object, Object> metadata = new HashMap<>();
            metadata.put("chatbotId", chatbotId);
            metadata.put("chatPrompt", prompt);
            metadata.put("status", ChatSessionStatus.IN_PROGRESS.name());
            metadata.put("total_token", 0);
            redisTemplate.opsForHash().putAll(sessionKey, metadata);

            // 첫 메시지 초기화
            String todayMessage = chatbot.getInitMent();
            redisTemplate.delete(messageKey);
            redisTemplate.opsForList().rightPush(messageKey,
                    ChatMessage.builder().role("assistant").content(todayMessage).build());

            log.info("세션 생성 완료 : userId = {}, chatbotId={}", userId, chatbotId);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패, e");
            throw new RedisUnavailableException("Redis 서버 연결 실패");
        }
    }

    @Override
    public ChatSessionStatus getChatSessionStatus(Integer userId) {
        try {
            String key = session_key + ":" + userId;
            String statusData = (String) redisTemplate.opsForHash().get(key, "status");

            if (statusData == null) {
                return null;
            }

            ChatSessionStatus status = ChatSessionStatus.valueOf(statusData);
            return status.isOngoing() ? status : null;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패", e);
            throw new RedisUnavailableException("redis 연결 실패 ");
        }
    }

    /**
     * UserId 에 해당하는 채팅 세션의 모든 메시지 조회
     */
    @Override
    public ChatMessageListWithTokenDTO getChatHistory(long userId) {
        try {
            String messageKey = message_key + ":" + userId;
            String sessionKey = session_key + ":" + userId;

            int maxToken = 2000;

            // 1. 채팅 로그 조회
            List<Object> rawMessages = redisTemplate.opsForList().range(messageKey, 0, -1);
            if (rawMessages == null) {
                return new ChatMessageListWithTokenDTO(new ArrayList<>(), 0, maxToken);
            }

            List<ChatMessageResponseDTO> result = new ArrayList<>();
            for (Object msj : rawMessages) {
                ChatMessage msg = (ChatMessage) msj;
                result.add(new ChatMessageResponseDTO(msg.getRole(), msg.getContent()));
            }

            // 2. 글자수 토큰 추출
            Integer token = (Integer) redisTemplate.opsForHash().get(sessionKey,"total_token");

            int total_token = (token != null) ? token : 0;

            // 3-1. 글자수 토큰이 0인 경우 return
            if (total_token == 0) {
                return new ChatMessageListWithTokenDTO(result, 0, maxToken);
            }

            // 3-2. 글자수 토큰이 0이 아닌 경우 퍼센테이지 return
            int limit_token = 2000;  // 글자수 제한
            int usageRate = (int) Math.round((double) total_token / limit_token * 100);

            return new ChatMessageListWithTokenDTO(result, usageRate, maxToken);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패", e);
            throw new RedisUnavailableException("redis 연결 실패 ");
        }
    }
}
