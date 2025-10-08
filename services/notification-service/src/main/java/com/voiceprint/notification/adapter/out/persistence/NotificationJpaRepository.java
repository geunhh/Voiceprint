package com.voiceprint.notification.adapter.out.persistence;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {


    @Query("""
        SELECT n from Notification n
        where n.userId = :userId
        and n.isRead = false
        and (:cursor is null or n.id < :cursor)
        order by n.id desc
        """)
    List<NotificationJpaEntity> findMyNotifications(
            @Param("userId") Integer userId,
            @Param("cursor") Long cursor, Pageable pageable);

    Optional<NotificationJpaEntity> findByIdAndUserId(Long notificationId, Integer userId);
}
