package com.voiceprint.backend.service.alarm;

import com.voiceprint.backend.api.alarm.RedisPublisher;
import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.api.alarm.dto.NotificationListWithCursorDTO;
import com.voiceprint.backend.domain.Entity.Notification;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.NotificationRepository;
import com.voiceprint.backend.domain.Repository.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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


    /**
     * 읽지 않은 알림 정보를 조회하는 메서드 (커서 기반 무한스크롤)
     */
    public NotificationListWithCursorDTO getUnreadNotifications(Long userId, Long cursor, int size) {
        PageRequest page = PageRequest.of(0,size+1);

        List<Notification> notifications = notificationRepository.findMyNotifications(userId,cursor,page);

        boolean hasNext = notifications.size() > size;

        if (hasNext) {
            notifications = notifications.subList(0,size);
            log.info("다음 알림 존재");
        } else {
            log.info("마지막 알람입니다.");
        }

        Long nextCursor = hasNext ? notifications.getLast().getId() : null;

        List<NotificationDTO> response = notifications.stream()
                .map(n -> {
                    Map<String ,Object> metadata = new HashMap<>();
                    metadata.put("notificationId", n.getId());

                    if (n.getMetadata().get("groupId") != null) metadata.put("groupId", n.getMetadata().get("groupId"));
                    if (n.getMetadata().get("diaryId") != null) metadata.put("diaryId", n.getMetadata().get("diaryId"));
                    if (n.getMetadata().get("status") != null) metadata.put("status", n.getMetadata().get("status"));

                    return new NotificationDTO(n.getType(), n.getMessage(), metadata);
                })
                .toList();

        return new NotificationListWithCursorDTO(response, nextCursor);



    }
}
