package com.voiceprint.backend.global.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

    /**
     * 처리되지 않은 (Pending) 이벤트를 발생 시간 순서대로 조회합니다.
     */
    List<OutboxEventJpaEntity> findByStatusOrderByOccurredAtAsc(OutboxEventJpaEntity.EventStatus status, Pageable pageable);
}
