package com.voiceprint.backend.chat.application.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.ai.domain.PromptFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatPromptFactory implements PromptFactory {
    private final RedisTemplate<String, Object> redis;
    private final ObjectMapper om;

    @Value("${chat.max-length:2000}")
    private int maxChatLength;

    @Value("${session.key}")
    private String session_key;

    @Value("${message.key}")
    private String message_key;

    public record ChatTurn(String role, String content) {}

    /**
     * Prompt 생성 메서드 - 세션기반
     */
    public Prompt buildChatPrompt(String userId, String userText) {
        String sessionKey = session_key + ":" + userId;
        String messageKey = message_key + ":" + userId;
        log.info("session key : {}",sessionKey);
        log.info("message key : {}",messageKey);

        // 1) 세션 메타데이터 로드
        Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
        log.info("session : {}",session);

        if (session == null || session.isEmpty())
            throw new IllegalStateException("세션 없음 or userId invalid");

        int totalToken = parseIntSafe(session.get("total_token"));
        if (totalToken > maxChatLength)
            throw new IllegalStateException("챗봇 토큰(대화 길이) 초과");

        String systemPrompt = (String) session.get("chatPrompt");
        log.info("systemPrompt : {}",systemPrompt);

        // 2) 히스토리 로드 (JSON -> ChatTurn)
        List<Object> raw = null; //. 뭐가 문제인거시야
        try {
            raw = redis.opsForList().range(messageKey,0,-1);
        } catch (Exception e) {
            log.error("Redis에서 대화 기록을 가져오는 중 오류 발생", e);
            throw e;
        }
        log.info("raw : {}", raw );
        List<Message> msgs = new ArrayList<>();

        log.info("msgs : {}",msgs);

        if (systemPrompt != null && !systemPrompt.isBlank())
            msgs.add(new SystemMessage(systemPrompt));
        log.info("msgs : {}",msgs);

        if (raw != null) {
            for (Object o : raw ) {
                try {
                    ChatTurn t = om.readValue(String.valueOf(o), ChatTurn.class);
                    if ("user".equalsIgnoreCase(t.role()))
                        msgs.add(new UserMessage(t.content()));
                    else if ("assistant".equalsIgnoreCase(t.role()))
                        msgs.add(new AssistantMessage(t.content()));
                } catch (Exception e) {
                    log.error("채팅 파싱 실패 {}",o, e);
                }
            }
        }

        // 3) 신규 유저 메시지 넣기.
        msgs.add(new UserMessage(userText));
        log.info("msgs : {}",msgs);

        //4) 모델 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.4)
                .build();
        log.info("options : {}",options);

        return new Prompt(msgs, options);
    }

    // 파싱 유틸
    private int parseIntSafe(Object v) {
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }

    }
}
