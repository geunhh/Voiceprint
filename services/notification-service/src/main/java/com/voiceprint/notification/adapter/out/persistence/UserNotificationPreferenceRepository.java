package com.voiceprint.notification.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreferenceJpaEntity, Long> {
    Optional<UserNotificationPreferenceJpaEntity> findByUserId(Integer userId);

    List<UserNotificationPreferenceJpaEntity> findByEnableAlarmsTrueAndAlarmTime(LocalTime alarmTime);


    List<UserNotificationPreferenceJpaEntity> findByEnableAlarmsTrueAndAlarmTimeAndUserIdIn(
            LocalTime alarmTime, Collection<Integer> userIds
    );

    List<UserNotificationPreferenceJpaEntity> findByEnableAlarmsTrue();
}
