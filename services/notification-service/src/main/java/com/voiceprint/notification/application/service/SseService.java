package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.NotificationDTO;
import com.voiceprint.notification.adapter.out.sse.SseEmitterManager;
import com.voiceprint.notification.application.port.in.SseUseCase;
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
public class SseService implements SseUseCase {
    private final SseEmitterManager emitterManager;

    // 클라이언트가 구독 요청할 때 호출
    @Override
    public SseEmitter subscribe(Integer userId) {

//        connectionPoolMonitor.logHikariStatus();
        return emitterManager.add(userId);
    }

    // 실제 알림을 전송하는 메서드 : 테스트용
    @Override
    public void sendNotification(Integer userId, String eventName, NotificationDTO payload) {
        emitterManager.sendTo(userId, eventName, payload);
    }

    // 구독중인 사용자 조회하는  : 그룹 용
    @Override
    public Set<Integer> getSubscribedUserIds() {
        return emitterManager.getSubscribedUserIds();
    }
}