package com.voiceprint.backend.service.chat;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ChatPromptFactory {
    private final RedisTemplate<String, Object> redis;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${chat.max-length:2000}")
    private int maxChatLength;

    public record ChatTurn(String role, String content) {}

    /**
     * Prompt 생성 메서드 - 세션기반
     */
    public Prompt buildChatPrompt(String userId, String userText) {
        // 1) 세션 정보 로드
        String sessionKey = "chat_session:" + userId;           //Todo : 바꾸기
        String messageKey = "chat_session_messages:" + userId;

        Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
        if (session == null || session.isEmpty())
            throw new IllegalStateException("세션 없음 or userId invalid");

        int totalToken = parseIntSafe(session.get("total_token"));
        if (totalToken > maxChatLength)
            throw new IllegalStateException("챗봇 토큰 초과");

        String systemPrompt = (String) session.get("chatPrompt");

        // 2) 히스토리 로드 (JSON -> ChatTurn)
        List<Object> raw = redis.opsForList().range(messageKey,0,-1);
        List<Message> msgs = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isBlank())
            msgs.add(new SystemMessage(systemPrompt));

        if (raw != null) {
            for (Object o : raw ) {
                try {
                    ChatTurn t = om.readValue(String.valueOf(o), ChatTurn.class);
                    if ("user".equalsIgnoreCase(t.role()))
                        msgs.add(new UserMessage(t.content()));
                    else if ("assistant".equalsIgnoreCase(t.role()))
                        msgs.add(new AssistantMessage(t.content()));
                } catch (Exception ignore) {}
            }
        }

        // 3) 신규 유저 메시지 넣기.
        msgs.add(new UserMessage(userText));

        //4) 모델 옵션
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.4)
                .build();

        return new Prompt(msgs, options);
    }

    private int parseIntSafe(Object v) {
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return 0; }

    }
}
