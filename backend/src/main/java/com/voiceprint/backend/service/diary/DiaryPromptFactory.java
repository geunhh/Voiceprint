package com.voiceprint.backend.service.diary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.domain.ai.PromptFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DiaryPromptFactory implements PromptFactory {

    private final RedisTemplate<String, Object> redis;
    private final RedisTemplate<String, String> redisStr;   // list<String>
    private final ObjectMapper om = new ObjectMapper();

    @Value("${session.key}")
    private String session_key;

    @Value("${message.key}")
    private String message_key;

    @Override
    public Prompt buildDiaryPrompt(String userId) {

        String sessionKey = session_key + ":" + userId;
        String messageKey = message_key + ":" + userId;

        Map<Object, Object> session = redis.opsForHash().entries(sessionKey);
        if (session == null || session.isEmpty()) throw new IllegalStateException("세션 없음");


        String themeDesc  = (String) session.get("themeDescription");
        String themeTitle = (String) session.get("themeTitle");
        String themeDiary = (String) session.get("themeDiary");
        String themePrompt= (String) session.get("themePrompt");
        String chatSystem = (String) session.get("chatPrompt");


        // 히스토리에서 유저 메시지만 이어붙이기
        List<String> msgs = redisStr.opsForList().range(messageKey, 0, -1);
        String userChat = mergeUser(msgs);

        String system = """
            당신은 유저의 채팅 기록을 바탕으로 일기를 작성하는 전문가입니다.
            아래 테마 정보를 반영하여 글쓰기 스타일을 맞춰 주세요.

            [Theme Title]
            %s

            [Theme Description]
            %s

            [Theme Prompt]
            %s

            [Diary Example]
            %s

            출력은 반드시 엄격한 JSON으로만 반환하세요. 다른 설명/메타 정보/코드블록은 절대 포함하지 마세요.
            JSON 키: title, emotion, diary
            emotion 값은 ["행복","설렘","피로","짜증","우울"] 중 하나
            제약: 약 700자, 일기체 재구성, 발랄/활기찬 톤, 문단은 diary 내부에서 \\n\\n
            """.formatted(themeTitle, themeDesc, themePrompt, themeDiary);

        if (!chatSystem.isBlank()) system += "\n[Additional System Prompt]\n" + chatSystem;

        String user = """
            다음은 유저의 채팅 기록입니다. 이를 바탕으로 일기를 작성하세요.

            채팅 기록:
            %s

            예시:
            {"title":"오늘의 작은 설렘","emotion":"설렘","diary":"..."}
            """.formatted(userChat);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.4)
                .build();

        return new Prompt(
                List.of(new SystemMessage(system), new UserMessage(user)),
                options
        );
    }

    private String mergeUser(List<String> raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String j : raw) {
            try {
                JsonNode n = om.readTree(j);
                if ("user".equalsIgnoreCase(n.path("role").asText())) {
                    sb.append(n.path("content").asText()).append('\n');
                }
            } catch (Exception ignore) {}
        }
        return sb.toString().trim();
    }
}