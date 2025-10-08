package com.voiceprint.notification.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreferenceJpaEntity, Long> {
    Optional<UserNotificationPreferenceJpaEntity> findByUserId(Integer userId);
}
