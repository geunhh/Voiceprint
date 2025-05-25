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
            @Param("userId") Integer userId,
            @Param("cursor") Long cursor, Pageable pageable);

    // 성능 테스트용 전체조회 코드
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId
        AND n.isRead = false
        ORDER BY n.id DESC
    """)
    List<Notification> findAllByUserId(@Param("userId") Integer userId);


    Optional<Notification> findByIdAndUserId(Long notificationId, Integer userId);

    /**
     * 조회하지않은 알림 개수
     */
    Long countByUserIdAndIsReadFalse(Integer userId);
}
