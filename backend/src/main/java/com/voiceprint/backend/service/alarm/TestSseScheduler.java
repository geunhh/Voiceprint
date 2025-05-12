package com.voiceprint.backend.service.alarm;

import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestSseScheduler {

    private final SseService sseService;

    private final Long testUserId = 999L;
    private final List<String> statuses = List.of(
            "WAITING",
            "IN_PROGRESS",
            "DIARY_CREATING",
            "DIARY_DONE",
            "ERROR",
            "DIARY_SAVED"
    );

    private final AtomicInteger index = new AtomicInteger(0);

    /**
     * 테스트용 스케줄러 – 5초마다 테스트 알림 전송
     */
    @Scheduled(fixedRate = 5000)
    public void sendTestReminder() {
        String status = statuses.get(index.getAndUpdate(i -> (i + 1) % statuses.size()));
        log.info("🔁 테스트 상태: {}", status);

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
            default -> null; // DIARY_SAVED는 무시
        };

        if (payload != null) {
            try {
                sseService.sendNotification(testUserId, "reminder", payload);
                log.info("[📤 전송 완료] userId={}, status={}", testUserId, status);
            } catch (Exception e) {
                log.warn("[❌ 전송 실패] userId={}, error={}", testUserId, e.getMessage());
            }
        } else {
            log.info("💤 상태가 DIARY_SAVED라 알림 생략");
        }

    }
}
