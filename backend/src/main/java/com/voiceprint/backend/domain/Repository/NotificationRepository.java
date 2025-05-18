package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {


    @Query("""
        SELECT  n from Notification n
        where n.user.id = :userId
        and n.isRead = false 
        and (:cursor is null or n.id < :cursor)
        order by n.id desc
        """)
    List<Notification> findMyNotifications(
            @Param("userId") Long userId,
            @Param("cursor") Long cursor, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long notificationId, Long userId);
}
