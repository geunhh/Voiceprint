package com.voiceprint.backend.user.application.port.out;

import com.voiceprint.backend.user.domain.User;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByProviderId(String providerId);
    boolean existsByNicknameAndIdNot(String nickname, Integer userId);
    List<User> findUsersWithAlarmEnabledAt(LocalTime alarmTime);
    Optional<User> findUserWithUsingThema(Integer userId);
    Optional<User> findById(Integer userId);

    User save(User user);
    void updateEnableAlarm(Integer userId, Boolean enable);
    void updateAlarmTime(Integer userId, LocalTime alarmTime);
    void updateProfile(Integer userId, String newNickname, Byte newProfileImageId);
}
