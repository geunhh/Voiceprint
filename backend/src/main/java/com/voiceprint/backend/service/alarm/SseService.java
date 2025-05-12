package com.voiceprint.backend.service.alarm;

import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.domain.alarm.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {
    private final SseEmitterManager emitterManager;

    // 클라이언트가 구독 요청할 때 호출
    public SseEmitter subscribe(Long userId) {
        return emitterManager.add(userId); // Emitter 생성 및 저장
    }

    // 실제 알림을 전송하는 메서드
    public void sendNotification(Long userId, String eventName, NotificationDTO payload) {
        emitterManager.sendTo(userId, eventName, payload);
    }

    // 구독중인 사용자 조회하는 메서드
    public Set<Long> getSubscribedUserIds() {
        return emitterManager.getSubscribedUserIds();
    }
}
