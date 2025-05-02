package com.voiceprint.backend.service.chat;

import com.voiceprint.backend.api.chat.dto.ChatMessage;
import com.voiceprint.backend.api.chat.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.common.exception.chat.RedisUnavailableException;
import com.voiceprint.backend.domain.auth.UserRepository;
import com.voiceprint.backend.domain.auth.Users;
import com.voiceprint.backend.domain.chat.ChatSessionStatus;
import com.voiceprint.backend.domain.chat.Chatbot;
import com.voiceprint.backend.domain.chat.ChatbotRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatbotRepository chatbotRepository;
    private final UserRepository userRepository;

    /**
     * 세션을 시작하는 메소드
     */
    public void startSession(Long userId, Long chatbotId) {
        String sessionKey = "chat_session:"+userId;
        String messageKey = "chat_session_messages:" + userId;

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
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
            user.setLastChatbot(chatbot);   // 최근 사용한 챗봇 저장
            userRepository.save(user);


            // 4. Redis 저장 : 챗봇ID, 챗봇 prompt, status
            Map<Object, Object> metadata = new HashMap<>();
            metadata.put("chatbotId", chatbotId);   //id
            metadata.put("chatPrompt", prompt);     //prompt
            metadata.put("status", ChatSessionStatus.IN_PROGRESS.name()); //status
            redisTemplate.opsForHash().putAll(sessionKey, metadata);

            // 첫 메시지 초기화
            String todayMessage = "오늘은 어떤일이 있었나요 >_< 꺄르륵 꺄르륵????";
            redisTemplate.delete(messageKey);
            redisTemplate.opsForList().rightPush(messageKey,
                    new ChatMessage("SERVER", todayMessage));

            log.info("세션 생성 완료 : userId = {}, chatbotId={}", userId, chatbotId);
        } catch (RedisConnectionFailureException e ) {
            log.error("Redis 연결 실패, e");
            throw new RedisUnavailableException("Redis 서버 연결 실패");
        }


    }

    public ChatSessionStatus getSessionStatus(Long userId) {
        try {
            String key = "chat_session:"+userId;
            String statusData = (String) redisTemplate.opsForHash().get(key,"status");
            System.out.println("key: "+ key);
            System.out.println("statusData: "+ statusData);

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
    public List<ChatMessageResponseDTO> getMessages(long userId) {
        try {
            String redisKey = "chat_session_messages:" + userId;
            List<Object> rawMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
            System.out.println(rawMessages);

            if (rawMessages == null) return new ArrayList<>();

            List<ChatMessageResponseDTO> result = new ArrayList<>();

            for (Object msj : rawMessages) {
                ChatMessage msg = (ChatMessage) msj;
                result.add(new ChatMessageResponseDTO(msg.getRole(), msg.getMessage()));
            }
            return result;
        }
        catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패",e);
            throw new RedisUnavailableException("redis 연결 실패 ");
        }

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
}
