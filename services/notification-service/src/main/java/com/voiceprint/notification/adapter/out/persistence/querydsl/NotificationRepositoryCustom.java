package com.voiceprint.notification.adapter.out.persistence.querydsl;

import com.voiceprint.notification.adapter.out.persistence.NotificationJpaEntity;

import java.util.List;

/**
 * NotificationRepository의 QueryDSL 커스텀 메서드 인터페이스
 * 
 * Spring Data JPA의 기본 메서드로 처리하기 어려운
 * 복잡한 동적 쿼리를 QueryDSL로 구현하기 위한 Custom 인터페이스
 */
public interface NotificationRepositoryCustom {

    /**
     * 사용자의 읽지 않은 알림 조회 (커서 기반 페이징)
     * 
     * @param userId 조회할 사용자 ID
     * @param cursor 이전 페이지의 마지막 알림 ID (null이면 첫 페이지)
     * @param limit  조회할 개수 (기본 10)
     * @return 읽지 않은 알림 목록 (최신순)
     */
    List<NotificationJpaEntity> findMyNotificationsWithQueryDsl(
            Integer userId,
            Long cursor,
            int limit);
}
