package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthProviderId(String providerId);

//    Optional<User> findByEmail(String email);

    boolean existsByNicknameAndIdNot(String nickname, Long userId);

    List<User> findByEnableAlarmIsTrueAndAlarmTime(LocalTime alarmTime);
}

