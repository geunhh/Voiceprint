package com.voiceprint.backend.service.chat;

import com.voiceprint.backend.api.chat.dto.ChatMessage;
import com.voiceprint.backend.api.chat.dto.ChatTextResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServcie {
    private final WebClient fastApiWebClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random(); // 임시
    private static final List<String> SAMPLE_RESPONSES = List.of( // 임시 메시지 리스트
            "헐 진짜요?? 정말 힘들었겠어요...",
            "아니 정말요??? 그런 일이 있었군요. 조금 더 이야기해 볼까요?",
            "헉.. 세상에나.. 제가 도와드릴 수 있을까요?",
            "잘 듣고 있어요. 어떤 일이 있었는지 알려주세요.",
            "뭐라고? 미친거 아니가??? 금마 그거 진짜 완전히 도라뿟네. 지금 어데있다노?"
    );


    public ChatTextResponseDTO processChat(Long userId, String message) {
        // requestBody 초기화
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userid",userId.toString());
        requestBody.put("chatting",message);

        // 변수 초기화
        Integer limit_token = 700;  // 글자수 제한
        String botResponse = "";    // 챗봇 답변
        Integer total_token = 0;    // 현재 글자수

        try {
            log.info("FastAPI 챗봇 호출 : {}",message);
            Map<String, Object> fastApiResponse = fastApiWebClient.post()
                    .uri("/api/v1/chat")
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

            if (fastApiResponse != null) {
                botResponse = fastApiResponse.get("chatting_response").toString();
                total_token = Integer.parseInt(fastApiResponse.get("token").toString());
                log.info("FastAPI 응답 : {}, 토큰 수 : {}", botResponse, total_token);
            }
        } catch (Exception e){
            log.error("FastAPI 호출 및 파싱에서 에러 : {}.",e.getMessage(),e);
        }

        // Redis 에 저장
        String sessionKey = "chat_session:" +userId;
        String redisKey = "chat_session_messages:" +userId;

        // redis에 현재 토큰수 저장
        redisTemplate.opsForHash().put(sessionKey,"total_token",total_token);

        // 레디스에 유저 request 저장
        ChatMessage userMsg = new ChatMessage("USER",message);
        redisTemplate.opsForList().rightPush(redisKey,userMsg);

        // 레디스에 서버 response 저장
        ChatMessage botMsg = new ChatMessage("SERVER",botResponse);
        redisTemplate.opsForList().rightPush(redisKey,botMsg);

        int usageRate = (int) Math.round((double) total_token / limit_token * 100);

        //2. 응답 처리
        return new ChatTextResponseDTO(botResponse,usageRate);
    }
}
