package com.voiceprint.backend.service.alarm;

import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryReminderScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final SseService sseService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 * * * * *") // 1분 마다 실행
    public void checkAndNotify() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0); // s와 ns 제거
        int minute = now.getMinute();
        log.info("알람 스케쥴러 시작 : {}",now);

        // 30분 단위 확인
        if (minute != 0 && minute !=30) {
            log.info("⏳ [{}:{}]은 알림 타이밍이 아님. 로직 종료.", now.getHour(), minute);
            return;
        }

        // 정확한 시간대면 -> 유저 조회
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // null 체크 유의.
            if ( user.getAlarmTime() == null || !Boolean.TRUE.equals(user.getEnableAlarm()) || !user.getAlarmTime().equals(now)) {
                continue;  // 알람 시간 없음, 알람 꺼짐, 알람 시각 아님 → 패스
            }
            Long userId = user.getId();
            String sessionKey = "chat_session:"+userId;

            Object statusObj = redisTemplate.opsForHash().get(sessionKey,"status");
            String status = (statusObj != null) ? statusObj.toString() : "NOT_EXIST";

            NotificationDTO payload = switch (status) {
                case "WAITING", "NOT_EXIST" -> new NotificationDTO(
                        "reminder",
                        "오늘은 일기 쓰셨나요? 지금 시작해보세요!",
                        "diary",
                        null
                );
                case "IN_PROGRESS" -> new NotificationDTO(
                        "reminder",
                        "대화가 아직 진행중이에요. 대화를 완료하고 일기를 생성해주세요!",
                        "diary",
                        null
                );
                case "DIARY_DONE", "DIARY_CREATING" -> new NotificationDTO(
                        "reminder", "생성된 일기가 있어요. 저장을 잊지 마세요!",
                        "diary", null
                );
                case "ERROR" -> new NotificationDTO(
                        "reminder", "일기 저장 중 오류가 발생했어요!",
                        "diary", null
                );
                default -> null; // DIARY_SAVED 알림 없음
            };

            if (payload != null) {
                try {
                    sseService.sendNotification(user.getId(), "reminder", payload);
                    log.info("[알림 전송 성공] userId={}, status={}", userId, status);
                } catch (Exception e) {
                    log.warn("[알림 전송 실패] userId={}, error={}", userId, e.getMessage());
                }
            }

        }
    }
}
