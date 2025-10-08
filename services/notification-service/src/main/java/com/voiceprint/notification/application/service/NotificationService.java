package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.adapter.in.web.dto.NotificationListWithCursorDTO;
import com.voiceprint.notification.adapter.out.RedisPublisher;
import com.voiceprint.notification.adapter.out.persistence.ProcessedEventJPARepository;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceJpaEntity;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceRepository;
import com.voiceprint.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.notification.domain.Notification;
import com.voiceprint.notification.application.port.in.NotificationCommandPort;
import com.voiceprint.notification.application.port.in.NotificationQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationService implements NotificationCommandPort, NotificationQueryPort {

    private final NotificationRepositoryPort notificationPort;
    private final RedisPublisher redisPublisher;
    private final UserNotificationPreferenceRepository userNotificationPreferenceRepository;
    private final ProcessedEventJPARepository processedEventRepository;

    /**
     * 알림 생성 + DB 저장 + Redis Pub/Sub 전송
     */
    @Override
    public void sendAndSave(UserNotificationPreferenceJpaEntity user, NotificationDTO dto) {
        Notification notification = Notification.create(
                user.getUserId(),
                dto.getType(),
                dto.getMessage(),
                dto.getMetadata()
        );
        Notification savedNotification = notificationPort.save(notification);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NotificationDTO inputDto = new NotificationDTO(
                        dto.getType(),
                        dto.getMessage(),
                        Map.of(
                                "notificationId", savedNotification.getId(),
                                "userId", user.getId()
                        )
                );
                redisPublisher.publishNotification(inputDto);
            }
        });
    }


    /**
     * 읽지 않은 알림 정보를 조회하는 메서드 (커서 기반 무한스크롤)
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationListWithCursorDTO getUnreadNotifications(Integer userId, Long cursor, Integer size) {
        List<Notification> notifications = notificationPort.findMyNotifications(userId, cursor, size + 1);

        boolean hasNext = notifications.size() > size;

        if (hasNext) {
            notifications = notifications.subList(0, size);
            log.info("다음 알림 존재");
        } else {
            log.info("마지막 알람입니다.");
        }

        Long nextCursor = hasNext ? notifications.get(notifications.size() - 1).getId() : null;

        List<NotificationDTO> response = notifications.stream()
                .map(n -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("notificationId", n.getId());

                    if (n.getMetadata() != null) {
                        if (n.getMetadata().get("groupId") != null)
                            metadata.put("groupId", n.getMetadata().get("groupId"));
                        if (n.getMetadata().get("diaryId") != null)
                            metadata.put("diaryId", n.getMetadata().get("diaryId"));
                        if (n.getMetadata().get("status") != null)
                            metadata.put("status", n.getMetadata().get("status"));
                    }

                    return new NotificationDTO(n.getType(), n.getMessage(), metadata);
                })
                .collect(Collectors.toList());

        return new NotificationListWithCursorDTO(response, nextCursor);
    }

    /**
     * 알림 읽음 처리 메서드
     */
    @Override
    public void markNotification(Integer userId, Long notificationId) {
        notificationPort.markAsRead(notificationId, userId);
        log.debug("noti Id : {} -  읽음 처리 성공", notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public void publishAllNotifications(List<Notification> notifications) {
        log.debug("알림 전송하기");
        for (Notification n : notifications) {
            log.debug("notification : {}", n.getMessage());
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("notificationId", n.getId());
            metadata.put("groupId", n.getMetadata() != null ? n.getMetadata().getOrDefault("groupId", null) : null);
            metadata.put("diaryId", n.getMetadata() != null ? n.getMetadata().getOrDefault("diaryId", null) : null);

            NotificationDTO dto = new NotificationDTO(
                    n.getType(),
                    n.getMessage(),
                    metadata
            );
            log.debug("notiDTO : {}", dto);
            log.debug("notiDTO meta : {}", dto.getMetadata());
            redisPublisher.publishNotification(dto);
        }
    }

    @Transactional
    public void updateNotificationMetadata(List<Notification> notifications) {
        log.warn("updateNotificationMetadata method needs implementation using NotificationPort.");
    }
}