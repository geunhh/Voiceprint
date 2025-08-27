package com.voiceprint.backend.chat.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.ai.domain.AiResult;
import com.voiceprint.backend.ai.domain.PromptFactory;
import com.voiceprint.backend.chat.adapter.out.persistence.ChatbotJPAEntity;
import com.voiceprint.backend.chat.domain.ChatMessage;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryThemaJpaEntity;
import com.voiceprint.backend.diary.adapter.out.persistence.EmotionJPAEntity;
import com.voiceprint.backend.notification.adapter.in.web.NotificationDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.*;
import com.voiceprint.backend.global.exception.chat.ChatSessionNotFoundException;
import com.voiceprint.backend.global.exception.chat.RedisUnavailableException;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.*;
import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import com.voiceprint.backend.chat.adapter.out.persistence.ChatbotRepository;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryEntity;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryRepository;
import com.voiceprint.backend.diary.adapter.out.persistence.EmotionRepository;
import com.voiceprint.backend.ai.domain.AiServicePort;
import com.voiceprint.backend.service.alarm.NotificationService;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.voiceprint.backend.domain.Entity.ChatSessionStatus.DIARY_SAVED;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ChatSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatbotRepository chatbotRepository;
    private final UserRepository userRepository; // UserRepsitory 병합시 수정하기
    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final WebClient fastApiWebClient;
    private final NotificationService notificationService;
    private final AiServicePort aiService;
    private final PromptFactory promptFactory;
    private final ObjectMapper objectMapper;

    public ChatSessionService(RedisTemplate<String, Object> redisTemplate, ChatbotRepository chatbotRepository,
                              UserRepository userRepository, DiaryRepository diaryRepository,
                              EmotionRepository emotionRepository, WebClient fastApiWebClient,
                              NotificationService notificationService, AiServicePort aiService, @Qualifier("diaryPromptFactory") PromptFactory promptFactory, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.chatbotRepository = chatbotRepository;
        this.userRepository = userRepository;
        this.diaryRepository = diaryRepository;
        this.emotionRepository = emotionRepository;
        this.fastApiWebClient = fastApiWebClient;
        this.notificationService = notificationService;
        this.aiService = aiService;
        this.promptFactory = promptFactory;
        this.objectMapper = objectMapper;
    }
    @Value("${session.key}")
    private String session_key;

    @Value("${message.key}")
    private String message_key;
    /**
     * 세션을 시작하는 메소드
     */
    @Transactional(readOnly = true)
    public void startSession(Integer userId, Byte chatbotId) {
        String sessionKey = session_key + ":" + userId;
        String messageKey = message_key + ":" + userId;

        // 1.Redis에 기존 세션 존재하는지 확인
            // 있으면, status 주기
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
            ChatbotJPAEntity chatbot = chatbotRepository.findById(chatbotId)
                    .orElseThrow(() -> new IllegalArgumentException("챗봇 없음"));
            String prompt = chatbot.getPrompt();

            // 3-1. 챗봇 사용 정보 저장
            UserJPAEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
            user.setLastChatbot(chatbot);   // 최근 사용한 챗봇 저장
            userRepository.save(user);


            // 4. Redis 저장 : 챗봇ID, 챗봇 prompt, status
            Map<Object, Object> metadata = new HashMap<>();
            metadata.put("chatbotId", chatbotId);   //id
            metadata.put("chatPrompt", prompt);     //prompt
            metadata.put("status", ChatSessionStatus.IN_PROGRESS.name()); //status
            metadata.put("total_token", 0);
            redisTemplate.opsForHash().putAll(sessionKey, metadata);

            // 첫 메시지 초기화
            String todayMessage = chatbot.getInitMent();
            redisTemplate.delete(messageKey);
            redisTemplate.opsForList().rightPush(messageKey,
                    ChatMessage.builder().role("assistant").content(todayMessage).build());

            log.info("세션 생성 완료 : userId = {}, chatbotId={}", userId, chatbotId);
        } catch (RedisConnectionFailureException e ) {
            log.error("Redis 연결 실패, e");
            throw new RedisUnavailableException("Redis 서버 연결 실패");
        }


    }

    public ChatSessionStatus getSessionStatus(Integer userId) {
        try {
            String key = session_key + ":" +userId;
            String statusData = (String) redisTemplate.opsForHash().get(key,"status");

            if (statusData == null){
                return null;
            }

            ChatSessionStatus status = ChatSessionStatus.valueOf(statusData);
            return status.isOngoing() ? status : null ;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패",e);
            throw new RedisUnavailableException("redis 연결 실패 ");
        }

    }

    /**
     * UserId 에 해당하는 채팅 세션의 모든 메시지 조회
     */
    public ChatMessageListWithTokenDTO getMessages(long userId) {
        try {
            // Redis Key.
            String messageKey = message_key + ":" + userId;
            String sessionKey = session_key + ":" + userId;

            int maxToken = 2000;

            // 1. 채팅 로그 조회
            List<Object> rawMessages = redisTemplate.opsForList().range(messageKey, 0, -1);
            log.debug("채팅로그 {} ", rawMessages);

            if (rawMessages == null) return new ChatMessageListWithTokenDTO(new ArrayList<>(),0, maxToken);

            List<ChatMessageResponseDTO> result = new ArrayList<>();

            for (Object msj : rawMessages) {
                ChatMessage msg = (ChatMessage) msj;
                result.add(new ChatMessageResponseDTO(msg.getRole(), msg.getContent()));
            }

            // 2. 글자수 토큰 추출
            Integer token = (Integer) redisTemplate.opsForHash().get(sessionKey,"total_token");

            int total_token = (token != null) ? token : 0;
            log.debug("token {}",total_token);

            // 3-1. 글자수 토큰이 0인 경우 return
            if (total_token == 0) {
                return new ChatMessageListWithTokenDTO(result, 0, maxToken) ;

            }

            // 3-2. 글자수 토큰이 0이 아닌 경우 퍼센테이지 return

            int limit_token = 2000;  // 글자수 제한
            int usageRate = (int) Math.round((double) total_token / limit_token * 100);

            return new ChatMessageListWithTokenDTO(result, usageRate, maxToken) ;
        }
        catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패",e);
            throw new RedisUnavailableException("redis 연결 실패 ");
        }

    }

    /**
     * 채팅 종료 메서드
     */
    public void endSession(Integer userId) {
        // 채팅 관련 Redis 키.
        final String sessionKey = session_key + ":" + userId;

        // 유저/테마 조회
        UserJPAEntity user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("유저 정보 없음"));
        DiaryThemaJpaEntity thema = user.getUsingThema();


        // 1. 상태값 갱신 + 테마 메타 저장
        redisTemplate.opsForHash().put(sessionKey,"status",ChatSessionStatus.DIARY_CREATING.name());
        redisTemplate.opsForHash().put(sessionKey,"themeTitle",thema.getTitle());
        redisTemplate.opsForHash().put(sessionKey,"themeDescription",thema.getDescription());
        redisTemplate.opsForHash().put(sessionKey,"themePrompt",thema.getPrompt());
        redisTemplate.opsForHash().put(sessionKey,"themeDiary",thema.getExample());

        // 2. 백그라운드에서 일기 생성 비동기 처리
        CompletableFuture.runAsync(() -> {
            try {
                // reqeustBody 초기화
                Map<String, Integer> requestBody = new HashMap<>();
                requestBody.put("user_id", userId);

                Map<String, Object> fastApiResponse = fastApiWebClient.post()
                        .uri("/api/v1/to_diary")
                        .bodyValue(requestBody)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                resp -> resp.bodyToMono(String.class).flatMap(body -> {
                                    log.error("FastAPI error [{}]: {}", resp.statusCode(), body);
                                    return Mono.error(new RuntimeException("FastAPI 호출 실패"));
                                })
                        )
                        .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {
                        })
                        .block();

                // FastAPI response 파싱
                String diary = fastApiResponse.get("diary").toString();
                String title = fastApiResponse.get("title").toString();
                String emotion = fastApiResponse.get("emotion").toString();

                // 일기 저장 및 상태 변경 => Redis
                redisTemplate.opsForHash().put(sessionKey,"tempDiary",diary);
                redisTemplate.opsForHash().put(sessionKey,"tempTitle",title);
                redisTemplate.opsForHash().put(sessionKey,"emotion",emotion);
                redisTemplate.opsForHash().put(sessionKey,"createdAt", LocalDateTime.now().toString()); //Todo:식간 -6
                // status 변경
                redisTemplate.opsForHash().put(sessionKey,"status",ChatSessionStatus.DIARY_DONE.name());
                log.info("일기 생성이 완료되었습니다.");

                // 알림 전송
                NotificationDTO dto = new NotificationDTO(
                        "diaryComplete",
                        "오늘의 일기가 생성이 완료되었습니다. 확인해보세요!!",
                        null
                );

                try {
                    notificationService.sendAndSave(user, dto);
                    log.info("[일기 생성 알림 전송] userId={}", user.getId());
                } catch (Exception e) {
                    log.error("[일기 생성 알림 실패] userId={}, err={}", user.getId(), e.getMessage());
                }
            }
            catch (Exception e) {
                log.error("일기 생성 중 에러발생 : {}",e.getMessage());
                redisTemplate.opsForHash().put(sessionKey,"status",ChatSessionStatus.ERROR.name());
                // 에러처리 ..
            }
        });

    }

    /**
     * FastAPI -> SpringAI
     */
    public void endSession2(Integer userId) {
        // 채팅 관련 Redis 키.
        final String sessionKey = session_key + ":" + userId;

        // 유저/테마 조회
        UserJPAEntity user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("유저 정보 없음"));
        DiaryThemaJpaEntity thema = user.getUsingThema();


        // 1. 상태값 갱신 + 테마 메타 저장
        redisTemplate.opsForHash().put(sessionKey,"status",ChatSessionStatus.DIARY_CREATING.name());
        redisTemplate.opsForHash().put(sessionKey,"themeTitle",thema.getTitle());
        redisTemplate.opsForHash().put(sessionKey,"themeDescription",thema.getDescription());
        redisTemplate.opsForHash().put(sessionKey,"themePrompt",thema.getPrompt());
        redisTemplate.opsForHash().put(sessionKey,"themeDiary",thema.getExample());

        // 2. 백그라운드에서 일기 생성 비동기 처리
        CompletableFuture.runAsync(() -> {
            try {
                // 2-1 프롬프트 생성 (Redis에서 대화 이력/테마 조회)
                Prompt prompt = promptFactory.buildDiaryPrompt(userId.toString());
                log.info("## 프롬프트 : {}",prompt.getContents());
                log.info("## 프롬프트 : {}",prompt.getUserMessages());
                log.info("## 프롬프트 : {}",prompt.getSystemMessage());


                // 2-2 Spring AI 호출
                AiResult ai = aiService.chat(prompt);
                String content = stripCodeFence(ai.getContent());

                // 2-3) JSON 파싱
                JsonNode node = objectMapper.readTree(content);
                String diary = node.path("diary").asText("");
                String title = node.path("title").asText("");
                String emotion = normalizeEmotion(node.path("emotion").asText(""));

                if (title.isBlank() || diary.isBlank()) {
                    throw new IllegalStateException("응답 형식이 올바르지 않음 (title/diary 누락)");
                }

                // 3) Redis 저장 & 상태 변경
                redisTemplate.opsForHash().put(sessionKey, "tempDiary", diary);
                redisTemplate.opsForHash().put(sessionKey, "tempTitle", title);
                redisTemplate.opsForHash().put(sessionKey, "emotion", emotion);
                redisTemplate.opsForHash().put(sessionKey, "createdAt", LocalDateTime.now().toString());
                redisTemplate.opsForHash().put(sessionKey, "status", ChatSessionStatus.DIARY_DONE.name());
                log.info("일기 생성이 완료되었습니다. userId={}", user.getId());

                // 4) 알림 전송
                NotificationDTO dto = new NotificationDTO(
                        "diaryComplete",
                        "오늘의 일기가 생성이 완료되었습니다. 확인해보세요!!",
                        null
                );

                try {
                    notificationService.sendAndSave(user, dto);
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



    public TempDiaryResponseDTO getTempDiary(Integer userId) {
        String sessionKey = session_key + ":"+userId;

        // 현재 상태 확인
        String status = (String) redisTemplate.opsForHash().get(sessionKey,"status");
        if (!ChatSessionStatus.DIARY_DONE.name().equals(status)) {
            throw new ChatSessionNotFoundException("아직 생성된 일기가 없습니다.");
        }

        String title = (String) redisTemplate.opsForHash().get(sessionKey,"tempTitle");
        String diary = (String) redisTemplate.opsForHash().get(sessionKey,"tempDiary");
        String createdAt = (String) redisTemplate.opsForHash().get(sessionKey, "createdAt");
        String emotion = (String) redisTemplate.opsForHash().get(sessionKey, "emotion");

        return new TempDiaryResponseDTO(title, diary, createdAt, emotion);
    }

    /**
     * 일기 생성 재시도 메서드
     */
    public void retryTempDiaryGeneration(Integer userId) {
        String sessionKey = session_key + ":" + userId;

        // 1. 기존 임시 일기 관련 필드 삭제
        redisTemplate.opsForHash().delete(sessionKey,
                "tempDiary", "tempTitle", "createdAt", "emotion", "status");
        // 2. 종료 로직 재사용
        endSession2(userId);
    }

    /**
     * 임시 다이어리 수정 메소드
     */
    public UpdateDiaryResult updateTempDiary(Integer userId, TempDiaryUpdateRequestDTO request) {
        String sessionKey = session_key + ":"+userId;

        Map<Object,Object> existing = redisTemplate.opsForHash().entries(sessionKey);
        System.out.println(existing);
        // 1. 존재하는가??
        if (existing == null || existing.isEmpty() || !existing.containsKey("tempDiary")) {
            throw new ChatSessionNotFoundException("수정할 임시 일기가 존재하지 않습니다.");
        }

        //2. 기존 데이터 추출
        String oldTitle = (String) existing.get("tempTitle");
        String oldDiary = (String) existing.get("tempDiary");
        String createdAt = (String) existing.get("createdAt");
        String emotion = (String) existing.get("emotion");

        boolean changed = false; // 변경 감지 변수

        // 3. 변경
        if (!Objects.equals(request.getTitle(), oldTitle)) {
            redisTemplate.opsForHash().put(sessionKey, "tempTitle",request.getTitle());
            changed = true;
        }
        if (!Objects.equals(request.getDiary(), oldDiary)) {
            redisTemplate.opsForHash().put(sessionKey, "tempDiary", request.getDiary());
            changed = true;
        }

        if (!changed) {
            log.info("임시 일기 변경 사항이 없습니다.");
            return new UpdateDiaryResult(changed, new TempDiaryResponseDTO(oldTitle,oldDiary,createdAt,emotion));
        }

        log.info("임시 일기가 수정되었습니다.");
        String updatedTitle = (String) redisTemplate.opsForHash().get(sessionKey, "tempTitle");
        String updatedDiary = (String) redisTemplate.opsForHash().get(sessionKey, "tempDiary");

        return new UpdateDiaryResult(changed, new TempDiaryResponseDTO(updatedTitle,updatedDiary,createdAt,emotion));



    }

    @Transactional(readOnly = false)
    public Integer confirmDiary(Integer userId) {
        String sessionKey = session_key + ":"+userId;
        String messageKey = message_key + ":"+userId;

        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);

        // 예외처리
        if (sessionData == null || sessionData.isEmpty() || !sessionData.containsKey("tempDiary")) {
            throw new ChatSessionNotFoundException("임시 일기가 존재하지 않습니다.");
        }

        // 1.Redis 세션 데이터 파싱
        String title = (String) sessionData.get("tempTitle");
        String content = (String) sessionData.get("tempDiary");
        Object chatbotIdObj = sessionData.get("chatbotId");
        String emotionStr = (String) sessionData.get("emotion"); // null일 수 있음
        String prompt = (String) sessionData.get("chatPrompt");


        // 2. Redis 메시지 파싱
        List<Object> rawMessages = redisTemplate.opsForList().range(messageKey,0,-1);

        List<ChatMessage> chatMessages = rawMessages.stream()
                .filter(ChatMessage.class::isInstance)
                .map(ChatMessage.class::cast)
                .collect(Collectors.toList());

        // 3. DB 조회
        UserJPAEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        EmotionJPAEntity emotion = (emotionStr != null)
                ? emotionRepository.findByName(emotionStr).orElse(null)
                : null;

        // 4. Diary 생성 및 저장
        DiaryEntity diaryEntity = DiaryEntity.createDiary(
                user,emotion,title,content,"임시..",prompt,chatMessages
        );

        //5. 최근 사용 챗봇 정보 저장
        Byte chatbotId = chatbotIdObj instanceof Number
                ? ((Number) chatbotIdObj).byteValue()
                : Byte.parseByte(String.valueOf(chatbotIdObj));

        ChatbotJPAEntity chatbot = chatbotRepository.findById(chatbotId)
                .orElseThrow(() -> new RuntimeException("챗봇 정보 없음"));
        user.setLastChatbot(chatbot);

        diaryRepository.save(diaryEntity);


        // 일기 생성 및 채팅 상태 변경
        redisTemplate.opsForHash().put(sessionKey,"status", DIARY_SAVED.name());


        return diaryEntity.getId();

    }
    // ------- 유틸 ---------//

    /**
     * ``` json ...``` 방어 메서드
     */
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

    /**
     * 감정 일반화 메서드
     */
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
