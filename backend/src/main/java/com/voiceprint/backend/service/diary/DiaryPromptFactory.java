package com.voiceprint.backend.service.diary;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DiaryPromptFactory {

    private final RedisTemplate<String, Object> redis;

    /** FastAPI /api/v1/to_diary: themeDescription/Title/Diary 사용 */
    public Prompt buildDiaryPrompt(String userId) {
        Map<Object, Object> session = redis.opsForHash().entries("chat_session:" + userId);

        String themeDesc  = (String) session.get("themeDescription");
        String themeTitle = (String) session.get("themeTitle");
        String themeDiary = (String) session.get("themeDiary");

        // 히스토리에서 유저 메시지만 이어붙이기
        List<Object> raw = redis.opsForList().range("chat_session_messages:" + userId, 0, -1);
        StringBuilder userChat = new StringBuilder();
        if (raw != null) {
            for (Object o : raw) {
                String s = String.valueOf(o);
                if (s.contains("\"role\":\"user\"")) {
                    int i = s.indexOf("\"content\":\"");
                    if (i > 0) {
                        String sub = s.substring(i + 11);
                        int j = sub.indexOf("\"");
                        if (j > 0) userChat.append(sub, 0, j).append('\n');
                    }
                }
            }
        }

        // FastAPI system prompt 재현 (요지 동일)
        String systemPrompt = """
            당신은 유저의 채팅 기록을 바탕으로 일기를 작성하는 전문가입니다. %s 글쓰기 스타일로 해주세요.

            다음 형식을 엄격하게 지켜서 일기를 만들어주세요:

            [일기의 제목]

            [행복/설렘/피로/짜증/우울] (다섯 가지 감정 중 하나만 선택)

            [채팅의 핵심 내용을 일기로 작성. 700자 내외로 작성]

            출력 예시:

            %s

            기쁨

            %s

            주의사항:
            1. 반드시 위 형식을 정확히 따라주세요
            2. 채팅 기록을 그대로 복사하지 말고, 일기 형태로 재구성해주세요
            3. 발랄하고 활기찬 분위기로 작성해주세요.
            4. 각 항목 사이에는 빈 줄을 넣어주세요
            5. 응답은 오직 일기 형식으로만 구성해주세요 (다른 설명이나 메타 정보 불필요)
            6. 글자 수는 700자 정도입니다.
        """.formatted(themeDesc, themeTitle, themeDiary);

        String userPrompt = """
            다음은 유저의 채팅 기록입니다. 이를 바탕으로 일기를 작성해주세요:

            %s
        """.formatted(userChat);

        var options = OpenAiChatOptions.builder()
                .model("gpt-4.1")
                .temperature(0.4)
                .build();

        return new Prompt(
                List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)),
                options
        );
    }
}