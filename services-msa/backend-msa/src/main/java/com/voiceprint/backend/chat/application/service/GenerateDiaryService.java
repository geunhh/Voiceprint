package com.voiceprint.backend.chat.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.ai.domain.AiResult;
import com.voiceprint.backend.ai.domain.AiServicePort;
import com.voiceprint.backend.ai.domain.PromptFactory;
import com.voiceprint.backend.chat.adapter.in.web.dto.TempDiaryResponseDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.TempDiaryUpdateRequestDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.UpdateDiaryResult;
import com.voiceprint.backend.chat.application.port.in.GenerateDiaryUseCase;
import com.voiceprint.backend.chat.application.port.out.ChatbotRepositoryPort;
import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.diary.application.port.out.EmotionRepositoryPort;

import com.voiceprint.backend.user.domain.User;
import com.voiceprint.backend.chat.domain.Chatbot;
import com.voiceprint.backend.chat.domain.ChatMessage;
import com.voiceprint.backend.chat.domain.ChatSessionStatus;
import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.diary.domain.DiaryThema;
import com.voiceprint.backend.diary.domain.Emotion;
import com.voiceprint.backend.global.exception.chat.ChatSessionNotFoundException;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.notification.adapter.in.web.NotificationDTO;
import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.voiceprint.backend.chat.domain.ChatSessionStatus.DIARY_SAVED;

@Service
@Slf4j
@Transactional(readOnly = true)
public class GenerateDiaryService implements GenerateDiaryUseCase {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatbotRepositoryPort chatbotRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final DiaryRepositoryPort diaryRepositoryPort;
    private final EmotionRepositoryPort emotionRepositoryPort;
    private final AiServicePort aiService;
    private final PromptFactory promptFactory;
    private final ObjectMapper objectMapper;

    public GenerateDiaryService(RedisTemplate<String, Object> redisTemplate, ChatbotRepositoryPort chatbotRepositoryPort, UserRepositoryPort userRepositoryPort, DiaryRepositoryPort diaryRepositoryPort, EmotionRepositoryPort emotionRepositoryPort, AiServicePort aiService, @Qualifier("diaryPromptFactory") PromptFactory promptFactory, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.chatbotRepositoryPort = chatbotRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.diaryRepositoryPort = diaryRepositoryPort;
        this.emotionRepositoryPort = emotionRepositoryPort;
        this.aiService = aiService;
        this.promptFactory = promptFactory;
        this.objectMapper = objectMapper;
    }

    @Value("${session.key}")
    private String session_key;

    @Value("${message.key}")
    private String message_key;

    @Override
    public void endChatSessionAndGenerateDiary(Integer userId) {
        final String sessionKey = session_key + ":" + userId;

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));
        DiaryThema thema = user.getUsingThema();

        redisTemplate.opsForHash().put(sessionKey, "status", ChatSessionStatus.DIARY_CREATING.name());
        redisTemplate.opsForHash().put(sessionKey, "themeTitle", thema.getTitle());
        redisTemplate.opsForHash().put(sessionKey, "themeDescription", thema.getDescription());
        redisTemplate.opsForHash().put(sessionKey, "themePrompt", thema.getPrompt());
        redisTemplate.opsForHash().put(sessionKey, "themeDiary", thema.getExample());

        CompletableFuture.runAsync(() -> {
            try {
                Prompt prompt = promptFactory.buildDiaryPrompt(userId.toString());
                AiResult ai = aiService.chat(prompt);
                String content = stripCodeFence(ai.getContent());
                log.info("AI Response Content: {}", content);

                JsonNode node = objectMapper.readTree(content);
                String diary = node.path("diaryEntity").asText("");
                String title = node.path("title").asText("");
                String emotion = normalizeEmotion(node.path("emotion").asText(""));

                log.info("Parsed Title: {}", title);
                log.info("Parsed Diary: {}", diary);

                if (title.isBlank() || diary.isBlank()) {
                    throw new IllegalStateException("응답 형식이 올바르지 않음 (title/diary 누락)");
                }

                redisTemplate.opsForHash().put(sessionKey, "tempDiary", diary);
                redisTemplate.opsForHash().put(sessionKey, "tempTitle", title);
                redisTemplate.opsForHash().put(sessionKey, "emotion", emotion);
                redisTemplate.opsForHash().put(sessionKey, "createdAt", LocalDateTime.now().toString());
                redisTemplate.opsForHash().put(sessionKey, "status", ChatSessionStatus.DIARY_DONE.name());
                log.info("일기 생성이 완료되었습니다. userId={}", user.getId());

                NotificationDTO dto = new NotificationDTO(
                        "diaryComplete",
                        "오늘의 일기가 생성이 완료되었습니다. 확인해보세요!!",
                        null
                );

                try {
                    // notificationService.sendAndSave(user, dto);
                    log.info("[일기 생성 알림 전송] userId={}", user.getId());
                } catch (Exception e) {
                    log.error("[일기 생성 알림 실패] userId={}, err={}", user.getId(), e.getMessage());
                }
            } catch (Exception e) {
                log.error("일기 생성 중 에러발생 : {}", e.getMessage(), e);
                redisTemplate.opsForHash().put(sessionKey, "status", ChatSessionStatus.ERROR.name());
                redisTemplate.opsForHash().put(sessionKey, "errorMessage", e.getMessage());
            }
        });
    }

    @Override
    public TempDiaryResponseDTO getTemporaryDiary(Integer userId) {
        String sessionKey = session_key + ":" + userId;

        String status = (String) redisTemplate.opsForHash().get(sessionKey, "status");
        if (!ChatSessionStatus.DIARY_DONE.name().equals(status)) {
            throw new ChatSessionNotFoundException("아직 생성된 일기가 없습니다.");
        }

        String title = (String) redisTemplate.opsForHash().get(sessionKey, "tempTitle");
        String diary = (String) redisTemplate.opsForHash().get(sessionKey, "tempDiary");
        String createdAt = (String) redisTemplate.opsForHash().get(sessionKey, "createdAt");
        String emotion = (String) redisTemplate.opsForHash().get(sessionKey, "emotion");

        return new TempDiaryResponseDTO(title, diary, createdAt, emotion);
    }

    @Override
    public void retryDiaryGeneration(Integer userId) {
        String sessionKey = session_key + ":" + userId;

        redisTemplate.opsForHash().delete(sessionKey,
                "tempDiary", "tempTitle", "createdAt", "emotion", "status");
        endChatSessionAndGenerateDiary(userId);
    }

    @Override
    public UpdateDiaryResult updateTemporaryDiary(Integer userId, TempDiaryUpdateRequestDTO request) {
        String sessionKey = session_key + ":" + userId;

        Map<Object, Object> existing = redisTemplate.opsForHash().entries(sessionKey);
        if (existing == null || existing.isEmpty() || !existing.containsKey("tempDiary")) {
            throw new ChatSessionNotFoundException("수정할 임시 일기가 존재하지 않습니다.");
        }

        String oldTitle = (String) existing.get("tempTitle");
        String oldDiary = (String) existing.get("tempDiary");
        String createdAt = (String) existing.get("createdAt");
        String emotion = (String) existing.get("emotion");

        boolean changed = false;

        if (!Objects.equals(request.getTitle(), oldTitle)) {
            redisTemplate.opsForHash().put(sessionKey, "tempTitle", request.getTitle());
            changed = true;
        }
        if (!Objects.equals(request.getDiary(), oldDiary)) {
            redisTemplate.opsForHash().put(sessionKey, "tempDiary", request.getDiary());
            changed = true;
        }

        if (!changed) {
            log.info("임시 일기 변경 사항이 없습니다.");
            return new UpdateDiaryResult(changed, new TempDiaryResponseDTO(oldTitle, oldDiary, createdAt, emotion));
        }

        log.info("임시 일기가 수정되었습니다.");
        String updatedTitle = (String) redisTemplate.opsForHash().get(sessionKey, "tempTitle");
        String updatedDiary = (String) redisTemplate.opsForHash().get(sessionKey, "tempDiary");

        return new UpdateDiaryResult(changed, new TempDiaryResponseDTO(updatedTitle, updatedDiary, createdAt, emotion));
    }

    @Override
    @Transactional
    public Integer confirmDiary(Integer userId) {
        String sessionKey = session_key + ":" + userId;
        String messageKey = message_key + ":" + userId;

        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);

        if (sessionData == null || sessionData.isEmpty() || !sessionData.containsKey("tempDiary")) {
            throw new ChatSessionNotFoundException("임시 일기가 존재하지 않습니다.");
        }

        String title = (String) sessionData.get("tempTitle");
        String content = (String) sessionData.get("tempDiary");
        Object chatbotIdObj = sessionData.get("chatbotId");
        String emotionStr = (String) sessionData.get("emotion");
        String prompt = (String) sessionData.get("chatPrompt");

        List<Object> rawMessages = redisTemplate.opsForList().range(messageKey, 0, -1);

        List<ChatMessage> chatMessages = rawMessages.stream()
                .filter(ChatMessage.class::isInstance)
                .map(ChatMessage.class::cast)
                .collect(Collectors.toList());

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        Emotion emotion = (emotionStr != null)
                ? emotionRepositoryPort.findByName(emotionStr).orElse(null)
                : null;

        Diary diary = Diary.builder()
                .userId(userId)
                .emotion(emotion)
                .title(title)
                .content(content)
                .build();

        Byte chatbotId = chatbotIdObj instanceof Number
                ? ((Number) chatbotIdObj).byteValue()
                : Byte.parseByte(String.valueOf(chatbotIdObj));

        Chatbot chatbot = chatbotRepositoryPort.findById(chatbotId)
                .orElseThrow(() -> new RuntimeException("챗봇 정보 없음"));
        // user.setLastChatbot(chatbot);

        diaryRepositoryPort.save(diary);

        redisTemplate.opsForHash().put(sessionKey, "status", DIARY_SAVED.name());

        return diary.getId();
    }

    private String stripCodeFence(String content) {
        if (content == null) return "";
        String s = content.trim();
        if (s.startsWith("```")) {
            int first = s.indexOf('\n');
            int last = s.lastIndexOf("```");
            if (first >= 0 && last > first)
                return s.substring(first + 1, last).trim();
        }
        return s;
    }

    private String normalizeEmotion(String emotion) {
        if (emotion == null) return "행복";
        String x = emotion.trim();
        if (x.contains("행복")) return "행복";
        if (x.contains("설렘")) return "설렘";
        if (x.contains("피로")) return "피로";
        if (x.contains("짜증")) return "짜증";
        if (x.contains("우울")) return "우울";
        return "행복";
    }
}
