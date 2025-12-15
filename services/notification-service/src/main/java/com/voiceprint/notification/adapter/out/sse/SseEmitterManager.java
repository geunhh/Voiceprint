package com.voiceprint.notification.adapter.out.sse;

import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component // DB를 다루는 게 아니라, Emitter들을 Map에 보관하고 관리하는 역할이라 범용적인 Component가 맞음.
public class SseEmitterManager {

    // 사용자 별로 연결된 SseEmitter를 저장하는 ConcurrentHashMap
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>(); // 멀티스레드 환경에서 안전하게 접근 가능

    public SseEmitterManager(MeterRegistry meterRegistry) {
        meterRegistry.gauge("sse_emitters_count", emitters, Map::size);
    }

    // 새로운 SSE 연결을 추가하는 메서드
    public SseEmitter add(Integer userId) {
        // 기존 emitter가 있다면, 먼저 종료하고 제거
        if (emitters.containsKey(userId)) {
            try {
                emitters.get(userId).complete(); // 서버 -> client 연결 강제 종료
            } catch (Exception ignored) {}
            emitters.remove(userId);
        }

        SseEmitter emitter = new SseEmitter(1_800_000L); //30분 타임아웃 설정
        emitters.put(userId, emitter);

        // 연결이 끊기거나 오류가 날 경우 emitter 제거
        emitter.onCompletion(() -> {
            log.info("emitter 연결 종료 자원정리");
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            emitter.complete(); // emitter 종료
        });
        emitter.onError((e) -> {
            emitters.remove(userId);
            emitter.completeWithError(e);
        });

        // Initial Event 전송 (연결 확인 및 버퍼 플러시)
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
            throw new RuntimeException("Initial SSE connection failed", e);
        }

        return emitter;
    }

    // 알람을 특정 유저에게 전송하는 메서드
    public void sendTo(Integer userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)    // 알림 타입 : ex) comment, reminder, completeTo...
                        .data(data));       // 보낼 메시지 내용
                log.debug("[SSE] userId {} msg 전송완료",userId);
            } catch (IOException e) {
                emitters.remove(userId); // 오류 발생 시 emitter 제거
                log.debug("[SSE] 전송 실패로 emitter 제거: userId=" + userId + ", error=" + e.getMessage());

            }
        }
    }

    // 모든 유저에게 브로드캐스트 (Load Test용)
    public void broadcast(String eventName, Object data) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
                log.debug("[SSE] Broadcast 실패로 emitter 제거: userId=" + userId);
            }
        });
    }

    // emitter 존재 여부 확인 (디버깅 or 상태 체크용)
    public boolean hasEmitter(Integer userId) {
        return emitters.containsKey(userId);
    }

    public Set<Integer> getSubscribedUserIds() {
        return emitters.keySet();
    }

    public String getAllEmitterIds() {
        return emitters.keySet().toString();
    }

    public int getEmitterCount() {
        return emitters.size();
    }

    // Heartbeat 전송 (30초마다)
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data(""));
            } catch (IOException e) {
                emitters.remove(userId);
                log.debug("[SSE] Heartbeat 전송 실패로 emitter 제거: userId=" + userId);
            }
        });
    }
}
