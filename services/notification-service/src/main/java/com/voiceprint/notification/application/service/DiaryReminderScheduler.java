package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceJpaEntity;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiaryReminderScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;
    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;
    private final NotificationMessageFactory notificationMessageFactory;

    @Value("${session.key}")
    private String session_key;

    @Scheduled(cron = "0 */30 * * * *", zone = "Asia/Seoul") // 30분 마다 실행
    public void checkAndNotify() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0); // '초'단위 이하 제거.
        int minute = now.getMinute();

        // 0 or 30분일 때만 실행
        if (minute != 0 && minute !=30) return;

        log.info("🕒 리마인더 스케줄러 실행 시작: {}", now);

        notifyIndividualUsers(now);
//        notifyGroupUsers(now);
    }

    private void notifyIndividualUsers(LocalTime now) {
        // 모든 유저 탐색
        List<UserNotificationPreferenceJpaEntity> users = userNotificationPreferenceRepository.findAll();

        // 유저마다 알림 생성 및 처리.
        for (UserNotificationPreferenceJpaEntity user : users) {
            try {
                log.debug("userId : {}", user.getId());

                if (user.getAlarmTime() == null ||
                        !Boolean.TRUE.equals(user.getEnableAlarms()) ||
                        !user.getAlarmTime().equals(now)) {

                    continue;  // 알람 시간 없음, 알람 꺼짐, 알람 시각 아님 → 패스
                }

                Integer userId = user.getUserId();
                String sessionKey = session_key + ":" + userId;

                String status = "NOT_EXIST";

                Boolean hasStatus = redisTemplate.opsForHash().hasKey(sessionKey, "status");

                if (hasStatus != null || hasStatus) {
                    Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
                    status = (statusObj != null) ? statusObj.toString().replaceAll("\"", "") : "NOT_EXIST";
                    log.debug("status : {}",status);
                }

                NotificationDTO payload = notificationMessageFactory.createNotification(status);
                log.debug("payload : {}",payload);

                if (payload != null) {
                    try {
                        notificationService.sendAndSave(user, payload);
                        log.debug("[알림 저장 및 전송 완료] userId={}, status={}", userId, status);
                    } catch (Exception e) {
                        log.error("[알림 전송 실패] userId={}, error={}", userId, e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.debug("error : {}" , e.getMessage());
            }

        }
    }
}