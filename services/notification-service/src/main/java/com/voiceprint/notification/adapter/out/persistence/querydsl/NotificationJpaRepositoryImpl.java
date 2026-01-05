package com.voiceprint.notification.adapter.out.persistence.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.voiceprint.notification.adapter.out.persistence.NotificationJpaEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.voiceprint.notification.adapter.out.persistence.QNotificationJpaEntity.notificationJpaEntity;

/**
 * NotificationRepositoryCustom 구현체
 * 
 * 주의: Spring Data JPA는 "{JpaRepository이름}Impl" 네이밍 규칙으로
 * Custom 구현체를 자동으로 찾음
 * -> 클래스명: NotificationJpaRepositoryImpl (NotificationJpaRepository + Impl)
 */
@RequiredArgsConstructor
public class NotificationJpaRepositoryImpl implements NotificationRepositoryCustom {

    // QueryDslConfig에서 등록한 JPAQueryFactory Bean 주입
    private final JPAQueryFactory queryFactory;

    /**
     * QueryDSL을 사용한 동적 알림 조회
     * 
     * 기존 JPQL의 `:cursor is null or` 조건을
     * BooleanExpression의 null 반환을 통해 간결하게 처리
     */
    @Override
    public List<NotificationJpaEntity> findMyNotificationsWithQueryDsl(Integer userId, Long cursor, int limit) {
        return queryFactory
                .selectFrom(notificationJpaEntity)
                .where(
                        notificationJpaEntity.userId.eq(userId),
                        notificationJpaEntity.isRead.isFalse(),
                        cursorLessThan(cursor))
                .orderBy(notificationJpaEntity.id.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 커서 기반 페이징을 위한 동적 조건
     * 
     * QueryDSL 주요 비교 연산자:
     * - eq : = (equal)
     * - ne : != (not equal)
     * - lt : < (less than)
     * - loe: <= (less or equal)
     * - gt : > (greater than)
     * - goe: >= (greater or equal)
     */
    private BooleanExpression cursorLessThan(Long cursor) {
        return cursor != null ? notificationJpaEntity.id.lt(cursor) : null;
    }
}
