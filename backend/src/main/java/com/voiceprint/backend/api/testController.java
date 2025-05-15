package com.voiceprint.backend.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.parser.Entity;
import java.util.Set;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/test")
public class testController {

    @Value("${session.key}")
    private String sessionKey;

    @Value("${message.key}")
    private String messageKey;

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/reset")
    public ResponseEntity<?> deleteAllSession() {

        Set<String> sessionKeys = redisTemplate.keys(sessionKey+":*");
        Set<String> messageKeys = redisTemplate.keys(messageKey+":*");
        log.debug("세션 키 : {}",sessionKeys);
        log.debug("채팅 키 : {}",messageKeys);

        // 세션 키 초기화
        if (sessionKeys != null && !sessionKeys.isEmpty()) {
            redisTemplate.delete(sessionKeys);
            log.info("삭제된 세션 키 수: {}", sessionKeys.size());
        }

        // 메시지 키 초기화
        if (messageKeys != null && !messageKeys.isEmpty()) {
            redisTemplate.delete(messageKeys);
            log.info("삭제된 메시지 키 수: {}", messageKeys.size());
        }
        return ResponseEntity.ok("");

    }


}
