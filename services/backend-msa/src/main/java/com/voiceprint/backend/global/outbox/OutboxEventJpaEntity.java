package com.voiceprint.backend.global.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Outbox 패턴을 위한 이벤트 저장 테이블 엔티티
 */
@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventJpaEntity {

    public enum EventStatus {
        PENDING, PUBLISHED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId; // 멱등키

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType; // 예: "User"

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId; // 예: userId

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // 예: "UserCreated", "UserUpdated"

    @Column(columnDefinition = "JSON", nullable = false)
    private String payload; // 도메인 이벤트 본문(JSON)

    @Column(columnDefinition = "JSON")
    private String headers; // 선택: traceId, source, producerVersion 등

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt; // 도메인 변경 발생 시각

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 브로커 발행 완료 시각

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.PENDING; // PENDING|PUBLISHED|FAILED

    @Column(nullable = false)
    private int attempts = 0; // 발행 시도 횟수

    @Column(name = "partition_key", length = 128)
    private String partitionKey; // Kafka key로 쓸 값(예: userId, email)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public OutboxEventJpaEntity(String eventId, String aggregateType, String aggregateId, String eventType, String payload, String headers, LocalDateTime occurredAt, String partitionKey) {
        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.headers = headers;
        this.occurredAt = occurredAt;
        this.partitionKey = partitionKey;
        this.attempts = 0;
    }

    public void markAsPublished() {
        this.status = EventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = EventStatus.FAILED;
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}
