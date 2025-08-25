package com.voiceprint.backend.service.alarm;

import com.voiceprint.backend.notification.adapter.in.web.NotificationDTO;
import com.voiceprint.backend.global.logger.ConnectionPoolMonitor;
import com.voiceprint.backend.domain.Repository.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SseService {
    private final SseEmitterManager emitterManager;
    private final ConnectionPoolMonitor connectionPoolMonitor;

    // 클라이언트가 구독 요청할 때 호출
    public SseEmitter subscribe(Integer userId) {

        connectionPoolMonitor.logHikariStatus();
        return emitterManager.add(userId); // Emitter 생성 및 저장
    }

    // 실제 알림을 전송하는 메서드 : 테스트용
    public void sendNotification(Integer userId, String eventName, NotificationDTO payload) {
        emitterManager.sendTo(userId, eventName, payload);
    }

    // 구독중인 사용자 조회하는  : 그룹 용
    public Set<Integer> getSubscribedUserIds() {
        return emitterManager.getSubscribedUserIds();
    }
}
