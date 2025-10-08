package com.voiceprint.backend.global.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.global.event.UserEvent;
import com.voiceprint.backend.user.application.port.out.UserEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Outbox 테이블을 주기적으로 폴링하여 저장된 이벤트를 실제 메시지 브로커로 발행하는 스케줄러
 * 이 클래스는 Outbox 패턴의 "Message Relay" 역할을 수행하여, 데이터베이스 트랜잭션과 메시지 발행을 분리하고
 * 시스템 장애 상황에서도 이벤트가 최소 한 번 이상 전달되도록 보장합니다. (At-Least-Once Delivery)
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final UserEventPort userEventPort; // 실제 Kafka 발행 로직을 담고 있는 어댑터
    private final ObjectMapper objectMapper;

    /**
     * 고정된 주기로 Outbox 테이블을 확인하여 'PENDING' 상태의 이벤트를 처리합니다.
     * 각 이벤트에 대해 발행을 시도하고, 성공 시 'PUBLISHED', 실패 시 'FAILED'로 상태를 업데이트합니다.
     */
    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    @Transactional
    public void processOutboxEvents() {
        // 처리 대기 중인 이벤트를 최대 100개까지 조회 (한 번에 너무 많은 이벤트를 처리하지 않도록 제한)
        List<OutboxEventJpaEntity> events = outboxEventRepository
            .findByStatusOrderByOccurredAtAsc(OutboxEventJpaEntity.EventStatus.PENDING, PageRequest.of(0, 100));

        for (OutboxEventJpaEntity event : events) {
            try {
                // 발행 시도 횟수 증가 -
                event.incrementAttempts();

                // 저장된 JSON 페이로드를 UserEvent 객체로 역직렬화
                UserEvent userEvent = objectMapper.readValue(event.getPayload(), UserEvent.class);

                // 이벤트 타입에 따라 적절한 Port(Adapter) 메소드를 호출하여 Kafka로 발행
                // 이때, event.getAggregateId()를 Kafka 메시지 키로 사용하여 순서를 보장
                final String messageKey = event.getAggregateId();

                switch (event.getEventType()) {
                    case "USER_REGISTERED":
                        userEventPort.sendUserCreatedEvent(userEvent, messageKey);
                        break;
                    case "USER_PROFILE_UPDATED":
                    case "USER_NOTIFICATION_PREFERENCES_UPDATED":
                        userEventPort.sendUserUpdatedEvent(userEvent, messageKey);
                        break;
                    case "USER_DELETED":
                        userEventPort.sendUserDeletedEvent(userEvent, messageKey);
                        break;
                    default:
                        log.warn("Unknown event type: {}", event.getEventType());
                        continue; // 처리할 수 없는 이벤트는 건너뛰기
                }

                // 발행 성공 시, 상태를 'PUBLISHED' 로 변경
                event.markAsPublished();

            } catch (JsonProcessingException e) {
                // 페이로드 역직렬화 실패. 이 경우는 복구 불가능한 오류로 간주하고 'FAILED' 처리.
                log.error("Failed to deserialize event payload for event id: {}. Marking as FAILED.", event.getId(), e);
                event.markAsFailed();

            } catch (Exception e) {
                // Kafka 발행 실패 등 일시적인 네트워크 오류일 수 있음.
                log.error("Failed to process outbox event id: {}. Retries: {}. Error: {}", event.getId(), event.getAttempts(), e.getMessage());
                
                // 일정 횟수 이상 재시도 후에도 실패하면 'FAILED' 상태로 변경하여 무한 재시도를 방지.
                if (event.getAttempts() >= 5) { // 예: 5회 이상 실패 시 FAILED 처리
                    event.markAsFailed();
                    log.warn("Event id: {} has failed {} times and will be marked as FAILED.", event.getId(), event.getAttempts());
                }
            }
        }
    }
}
