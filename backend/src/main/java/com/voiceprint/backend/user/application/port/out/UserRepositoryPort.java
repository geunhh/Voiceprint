package com.voiceprint.backend.user.application.port.out;

import com.voiceprint.backend.user.domain.User;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByProviderId(String providerId);
    boolean existsByNicknameAndIdNot(String nickname, Integer userId);
    User save(User user);
    List<User> findUsersWithAlarmEnabledAt(LocalTime alarmTime);
    Optional<User> findUserWithUsingThema(Integer userId);
    Optional<User> findById(Integer userId);

}
