package com.voiceprint.backend.service.chat;

import com.voiceprint.backend.api.chat.dto.ChatMessage;
import com.voiceprint.backend.api.chat.dto.ChatTextResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServcie {
    private final WebClient fastApiWebClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${session.key}")
    private String session_key;

    @Value("${message.key}")
    private String message_key;

    public ChatTextResponseDTO processChat(Long userId, String message) {
        // requestBody 초기화
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userid",userId.toString());
        requestBody.put("chatting",message);

        // 변수 초기화
        int limit_token = 700;  // 글자수 제한
        String botResponse = "오류가 발생했습니다.";    // 챗봇 답변
        int total_token = 0;    // 현재 글자수

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
        String sessionKey = session_key + ":" +userId;
        String messageKey = message_key + ":" +userId;

        // redis에 현재 토큰수 저장
        redisTemplate.opsForHash().put(sessionKey,"total_token",total_token);

        // 레디스에 유저 request 저장
        ChatMessage userMsg = new ChatMessage("USER",message);
        redisTemplate.opsForList().rightPush(messageKey,userMsg);

        // 레디스에 서버 response 저장
        ChatMessage botMsg = new ChatMessage("SERVER",botResponse);
        redisTemplate.opsForList().rightPush(messageKey,botMsg);

        int usageRate = (int) Math.round((double) total_token / limit_token * 100);

        //2. 응답 처리
        return new ChatTextResponseDTO(botResponse,usageRate);
    }
}
