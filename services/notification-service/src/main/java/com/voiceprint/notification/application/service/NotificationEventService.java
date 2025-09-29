package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.out.RedisPublisher;
import com.voiceprint.notification.adapter.out.persistence.ProcessedEvent;
import com.voiceprint.notification.adapter.out.persistence.ProcessedEventJPARepository;
import com.voiceprint.notification.application.port.in.NotificationUseCase;
import com.voiceprint.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.notification.domain.Notification;
import com.voiceprint.notification.dto.NotificationDTO;
import com.voiceprint.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceJpaEntity;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceRepository;
import java.time.LocalTime;
import java.util.Map;

@Service
@Primary // This will be the primary implementation of NotificationUseCase
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationEventService implements NotificationUseCase {

    private final NotificationRepositoryPort notificationPort;
    private final RedisPublisher redisPublisher;
    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;
    private final ProcessedEventJPARepository processedEventRepository;

    @Override
    public void handleNotificationEvent(NotificationEvent event) {

        final String eventId = event.getEventId();
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("missing eventId");
        }

        // 1) 이미 처리했으면 스킵 (중복 알람 방지)
        if (processedEventRepository.existsById(eventId)) {
            log.info("Skip duplicate event: {}", eventId);
            return;
        }

        // 2) 비즈니스 처리.
        Notification notification = Notification.create(
                event.getRecipientId(),
                event.getType(),
                event.getMessage(),
                event.getMetadata()
        );
        Notification savedNotification = notificationPort.save(notification);

        // 3) 커밋 후 Redis publish
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NotificationDTO redisDto = new NotificationDTO(
                        savedNotification.getType(),
                        savedNotification.getMessage(),
                        Map.of(
                                "notificationId", savedNotification.getId(),
                                "userId", savedNotification.getUserId()
                        )
                );
                redisPublisher.publishNotification(redisDto);
            }
        });

        // 4) 성공 후 멱등 마킹
        processedEventRepository.save(ProcessedEvent.of(eventId));
    }

    @Override
    public void markNotification(Integer userId, Long notificationId) {

    }

    @Override
    public void publishAllNotifications(List<Notification> notifications) {

    }

    @Override
    public void handleUserRegisteredEvent(Integer userId, String nickname, String email) {
        // 새 사용자 등록 시 기본 알림 설정 생성
        UserNotificationPreferenceJpaEntity newPreference = UserNotificationPreferenceJpaEntity.builder()
                .userId(userId)
                .enableAlarms(false) // 기본적으로 알림 비활성화
                .alarmTime(LocalTime.of(21, 0)) // 기본 알림 시간 21:00
                .build();
        userNotificationPreferenceRepository.save(newPreference);
        log.info("User registered event handled: userId={}, nickname={}, email={}", userId, nickname, email);
    }

    @Override
    public void handleUserProfileUpdatedEvent(Integer userId, String nickname, String email) {
        // 프로필 업데이트 시 알림 설정에는 직접적인 영향 없음 (필요시 로직 추가)
        log.info("User profile updated event handled: userId={}, nickname={}, email={}", userId, nickname, email);
    }

    @Override
    public void handleUserNotificationPreferencesUpdatedEvent(Integer userId, Boolean enableAlarms, LocalTime alarmTime) {
        // 사용자 알림 설정 업데이트
        UserNotificationPreferenceJpaEntity preference = userNotificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 알림 설정이 없는 경우 새로 생성 (예: 기존 사용자인데 알림 설정이 없었던 경우)
                    return UserNotificationPreferenceJpaEntity.builder()
                            .userId(userId)
                            .enableAlarms(true) // 기본값
                            .alarmTime(LocalTime.of(21, 0)) // 기본값
                            .build();
                });

        if (enableAlarms != null) {
            preference.setEnableAlarms(enableAlarms);
        }
        if (alarmTime != null) {
            preference.setAlarmTime(alarmTime);
        }
        userNotificationPreferenceRepository.save(preference);
        log.info("User notification preferences updated event handled: userId={}, enableAlarms={}, alarmTime={}", userId, enableAlarms, alarmTime);
    }
}
