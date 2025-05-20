package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Group;
import com.voiceprint.backend.domain.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByProviderId(String providerId);

//    Optional<User> findByEmail(String email);

    boolean existsByNicknameAndIdNot(String nickname, Integer userId);

    List<User> findByEnableAlarmIsTrueAndAlarmTime(LocalTime alarmTime);

    /**
     * 알람여부가 True인 사용자만 조회
     */
    @Query("""
        select gu.user from GroupUser gu
        where gu.group = :group
        and gu.user.enableAlarm = true
    """)
    List<User> findAlarmEnabledUsersByGroup(
            @Param("group") Group group);
}

