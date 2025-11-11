package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("reminderNotificationMessageFactory")
public class ReminderNotificationMessageFactory implements NotificationMessageFactory {

    @Override
    public NotificationDTO createNotification(String status) {
        return switch (status) {
            case "WAITING", "NOT_EXIST" -> new NotificationDTO(
                    "reminder",
                    "오늘은 일기 쓰셨나요? 말자국으로 오늘 하루를 기록해 보세요!!",
                    Map.of("status", status)
            );
            case "IN_PROGRESS" -> new NotificationDTO(
                    "reminder",
                    "대화가 아직 진행중이에요. 대화를 완료하고 일기를 생성해주세요!",
                    Map.of("status", status)
            );
            case "DIARY_DONE", "DIARY_CREATING" -> new NotificationDTO(
                    "reminder", "생성된 일기가 있어요. 저장을 잊지 마세요!",
                    Map.of("status", status)
            );
            case "ERROR" -> new NotificationDTO(
                    "reminder", "일기 저장 중 오류가 발생했어요!",
                    Map.of("status", status)
            );
            default -> null; // DIARY_SAVED 알림 없음
        };
    }
}