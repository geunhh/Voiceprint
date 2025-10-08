package com.voiceprint.backend.notification.application.service;

import com.voiceprint.backend.notification.application.port.in.AlarmUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlarmService implements AlarmUseCase {

    @Value("${session.key}")
    private String sessionKey;

    @Value("${message.key}")
    private String messageKey;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 정해진 시간이 되면 redis Server에 저장된 세션 및 채팅 데이터 전체 삭제.
     */
    @Override
    @Scheduled(cron = "0 0 8 * * *") // 초, 분, 시, 일, 월, 요일,
    public void deleteAllSession() {
        log.debug("[스케쥴러] 자정 스케쥴러 실행: Redis 세션 삭제 시작");

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

    }
}