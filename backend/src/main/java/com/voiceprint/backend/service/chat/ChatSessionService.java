package com.voiceprint.backend.service.chat;

import com.nimbusds.jose.shaded.gson.Gson;
import com.voiceprint.backend.api.chat.dto.*;
import com.voiceprint.backend.common.exception.chat.ChatSessionNotFoundException;
import com.voiceprint.backend.common.exception.chat.RedisUnavailableException;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.domain.Entity.ChatSessionStatus;
import com.voiceprint.backend.domain.Entity.Chatbot;
import com.voiceprint.backend.domain.Repository.ChatbotRepository;
import com.voiceprint.backend.domain.Entity.Diary;
import com.voiceprint.backend.domain.Repository.DiaryRepository;
import com.voiceprint.backend.domain.Entity.Emotion;
import com.voiceprint.backend.domain.Repository.EmotionRepository;
import com.voiceprint.backend.domain.Entity.DiaryThema;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
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

import static com.voiceprint.backend.domain.Entity.ChatSessionStatus.DIARY_SAVED;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatbotRepository chatbotRepository;
    private final UserRepository userRepository; // UserRepsitory 병합시 수정하기
    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final WebClient fastApiWebClient;

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
            Chatbot chatbot = chatbotRepository.findById(chatbotId)
                    .orElseThrow(() -> new IllegalArgumentException("챗봇 없음"));
            String prompt = chatbot.getPrompt();

            // 3-1. 챗봇 사용 정보 저장
            User user = userRepository.findById(userId)
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
                    new ChatMessage("assistant", todayMessage));

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

            int maxToken = 700;

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

            int limit_token = 700;  // 글자수 제한
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
        String sessionKey = session_key + ":" + userId;

        // 일기 생성 직전 유저가 선택한 일기 테마를 Redis 서버에 갱신해줌
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("유저 정보 없음"));

        DiaryThema thema = user.getUsingThema();
        String cur_thema_title = thema.getTitle();
        String cur_thema_prompt = thema.getPrompt();
        String cur_thema_description = thema.getDescription();
        String cur_thema_example = thema.getExample();

        // 1. 상태값을 DIARY_CREATING(일기 생성중)으로 갱신
        redisTemplate.opsForHash().put(sessionKey,"status",ChatSessionStatus.DIARY_CREATING.name());

        // 1.5 일기 생성에 필요한 테마 정보 갱신
        redisTemplate.opsForHash().put(sessionKey,"themeTitle",cur_thema_title);
        redisTemplate.opsForHash().put(sessionKey,"themeDescription",cur_thema_description);
        redisTemplate.opsForHash().put(sessionKey,"themePrompt",cur_thema_prompt);
        redisTemplate.opsForHash().put(sessionKey,"themeDiary",cur_thema_example);

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
            }
            catch (Exception e) {
                log.error("일기 생성 중 에러발생 : {}",e.getMessage());
                redisTemplate.opsForHash().put(sessionKey,"status",ChatSessionStatus.ERROR.name());
                // 에러처리 ..
            }
        });

    }

    /**
     * 임시 일기 데이터
     */
    private String generateDiary(List<Object> messages) {
        return "오늘은 친구들이랑 한강 공원에 놀러갔다. " +
                "바람이 솔솔 불고 햇빛도 따뜻해서 걷기만 해도 기분이 좋아졌다. " +
                "엽떡에 유부 추가 3번, 허니콤보랑 시원한 맥주까지… 진짜 완벽한 조합! " +
                "잔디밭에 앉아 수다 떨고 먹는 그 시간이 참 좋았다. 해가 지고 나서는 따릉이를 타고 한강을 달렸다. " +
                "밤공기 맞으며 자전거 타는 기분, 요즘 같은 날씨에 최고다. " +
                "평범하지만 특별했던 하루. 이런 날, 자주 있었으면 좋겠다.";
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
        endSession(userId);
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
        List<Object> messages = redisTemplate.opsForList().range(messageKey,0,-1);
        String messagesJson = new Gson().toJson(messages);

        // 3. DB 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        Emotion emotion = (emotionStr != null)
                ? emotionRepository.findByName(emotionStr).orElse(null)
                : null;

        // 4. Diary 생성 및 저장
        Diary diary = Diary.createDiary(
                user,emotion,title,content,"임시..",prompt,messagesJson
        );

        //5. 최근 사용 챗봇 정보 저장
        Byte chatbotId = chatbotIdObj instanceof Number
                ? ((Number) chatbotIdObj).byteValue()
                : Byte.parseByte(String.valueOf(chatbotIdObj));

        Chatbot chatbot = chatbotRepository.findById(chatbotId)
                .orElseThrow(() -> new RuntimeException("챗봇 정보 없음"));
        user.setLastChatbot(chatbot);

        diaryRepository.save(diary);


        // 일기 생성 및 채팅 상태 변경
        redisTemplate.opsForHash().put(sessionKey,"status", DIARY_SAVED.name());


        return diary.getId();

    }
}
