package com.voiceprint.backend.service.alarm;

import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryReminderScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final SseService sseService;
    private final UserRepository userRepository;

    @Scheduled(fixedRate = 10000)
    public void checkAndNotify() {
        log.info("알람 스케쥴러 시작");
        // 유저 조회
        List<User> users = userRepository.findAll();

        for (User user : users) {
            Long userId = user.getId();
            String sessionKey = "chat_session:"+userId;

            Object statusObj = redisTemplate.opsForHash().get(sessionKey,"status");
//            String status = (statusObj != null) ? statusObj.toString() : "NOT_EXIST";
            String status = "IN_PROGRESS";

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
