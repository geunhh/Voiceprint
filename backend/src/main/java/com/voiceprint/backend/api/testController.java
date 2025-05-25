package com.voiceprint.backend.api;

import com.voiceprint.backend.service.alarm.DiaryReminderScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
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
    private final DiaryReminderScheduler diaryReminderScheduler;

    /**
     * 스케쥴러 테스트 API
     */
    @PostMapping("/scheduler/trigger")
    public ResponseEntity<String> triggerReminder() {
        LocalTime fakeNow = LocalTime.of(21,0); //00:00 rhwjd
        log.debug("fake now : {}",fakeNow);
        diaryReminderScheduler.checkAndNotifyTest(fakeNow);
        return ResponseEntity.ok("✅ 스케줄러 수동 실행 완료!");
    }

    /**
     * REdis 세션 초기화 API
     */
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
