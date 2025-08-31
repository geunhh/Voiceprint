package com.voiceprint.backend.user.adapter.out.persistence;

import com.voiceprint.backend.group.adapter.out.persistence.GroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserJPAEntity, Integer> {
    Optional<UserJPAEntity> findByProviderId(String providerId);

//    Optional<User> findByEmail(String email);

    boolean existsByNicknameAndIdNot(String nickname, Integer userId);

    List<UserJPAEntity> findByEnableAlarmIsTrueAndAlarmTime(LocalTime alarmTime);

    /**
     * 알람여부가 True인 사용자만 조회
     */
    @Query("""
        select gu.user from GroupUser gu
        where gu.group = :group
        and gu.user.enableAlarm = true
    """)
    List<UserJPAEntity> findAlarmEnabledUsersByGroup(
            @Param("group") GroupJpaEntity group);

    @Query("""
        select u from User u
        join fetch u.usingThema
        where u.id = :userId
    """)
    Optional<UserJPAEntity> findUserWithUsingThema(
            @Param("userId") Integer userId);


}

