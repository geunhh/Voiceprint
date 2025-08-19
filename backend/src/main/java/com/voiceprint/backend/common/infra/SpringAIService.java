package com.voiceprint.backend.common.infra;

import com.voiceprint.backend.domain.ai.AiResult;
import com.voiceprint.backend.domain.ai.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpringAIService implements AiService {

    private final ChatClient chatClient;

    @Override
    public AiResult chat(String userId, String userMessage) {

        try {
            // 모델 init
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model("gpt-4.1-mini")
                    .temperature(0.4)
                    .build();

            // 프롬프트
            Prompt prompt = new Prompt(
                    List.of(
                            new SystemMessage("You are a my friends"),
                            new UserMessage(userMessage)
                    ),
                    options);

            ChatResponse resp = chatClient.prompt(prompt).call().chatResponse();
            log.info("chatresp : {}", resp);


            return AiResult.builder()
                    .content(prompt.getContents())
                    .build();



        } catch (Exception e) {
            log.error("Spring AI call failed: {}", e.getMessage(), e );
            // 예외 대신 기본값 반환.
            return AiResult.builder()
                    .content("오류가 발생했습니다.")
                    .totalTokens(0)
                    .build();
        }
    }

    /**
     * 프롬프트 버전
     */
    @Override
    public AiResult chat(Prompt prompt) {
        try {
            log.info("하아..");

            var call = chatClient.prompt(prompt).call();
            String content = call.content();
//            ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();

            int totalTokens = 0;

            return AiResult.builder()
                    .content(content != null ? content : "")
                    .totalTokens(totalTokens)
                    .build();

        } catch (Exception e) {
            log.error("SpringAI call failed : {}", e.getMessage(), e);
            return AiResult.builder().content("오류 발생").totalTokens(0).build();
        }
    }

    @SuppressWarnings("unchecked")
    private int extractTotalTokens(Map<String, Object> metadata) {
        if (metadata == null) return 0;
        Object usageObj = metadata.get("usage");
        if (!(usageObj instanceof Map<?, ?> usage)) return 0;
        Object total = usage.get("total_tokens");
        if (total instanceof Number n) return n.intValue();
        if (total instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignore) {}
        }
        return 0;
    }
}
