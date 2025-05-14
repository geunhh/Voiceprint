package com.voiceprint.backend.service.alarm;

import com.voiceprint.backend.api.alarm.RedisPublisher;
import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.domain.Entity.Notification;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.NotificationRepository;
import com.voiceprint.backend.domain.Repository.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisPublisher redisPublisher;      // Redis Pub/sub관리
    private final SseEmitterManager sseEmitterManager;  // 현재 접속 유저 관리

    /**
     * 알림 생성 + DB 저장 + Redis Pub/Sub 전송
     */
    public void sendAndSave(User user, NotificationDTO dto) {
        // 1.DB 저장
        Notification notification = Notification.create(
                user,
                dto.getType(),
                dto.getMessage(),
                dto.getMetadata()
        );
        notificationRepository.save(notification);

        // 2. Redis 전송
        NotificationDTO inputDto = new NotificationDTO(
                dto.getType(),
                dto.getMessage(),
                Map.of("notificationId", notification.getId())
        );
        redisPublisher.publishNotification(inputDto);
    }


}
