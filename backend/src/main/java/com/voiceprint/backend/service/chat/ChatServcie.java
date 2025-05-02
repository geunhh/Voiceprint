package com.voiceprint.backend.service.chat;

import com.voiceprint.backend.api.chat.dto.ChatMessage;
import com.voiceprint.backend.api.chat.dto.ChatTextResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServcie {
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
        //TODO: FastAPI 파이프라인으로 대체 예정.
        String sessionKey = "chat_session:" +userId;
        String redisKey = "chat_session_messages:" +userId;

        // 레디스에 유저 request 저장
        ChatMessage userMsg = new ChatMessage("USER",message);
        redisTemplate.opsForList().rightPush(redisKey,userMsg);

        // 임시 답변 생성
        String botResponse = SAMPLE_RESPONSES.get(random.nextInt(SAMPLE_RESPONSES.size()));

        // 레디스에 서버 response 저장
        ChatMessage botMsg = new ChatMessage("SERVER",botResponse);
        redisTemplate.opsForList().rightPush(redisKey,botMsg);

        //1. FastAPI로 요청 전송

        //2. 응답 처리
        return new ChatTextResponseDTO(botResponse,50);
    }
}
