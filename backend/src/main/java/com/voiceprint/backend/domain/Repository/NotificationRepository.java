package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByUserIdAndIsReadFalse(Long userId);
}
