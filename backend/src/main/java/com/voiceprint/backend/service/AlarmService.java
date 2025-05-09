package com.voiceprint.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {

    @Value("${session.Key}")
    private String sessionKey;

    @Value("${message.Key}")
    private String messageKey;

    private RedisTemplate redisTemplate;

    /**
     * 정해진 시간이 되면 redis Server에 저장된 세션 및 채팅 데이터 전체 삭제.
     */
    @Scheduled(cron = "0 30 13 * * *")
    public void testRun() {
        log.debug("알람 테스트");

        //

    }
}
