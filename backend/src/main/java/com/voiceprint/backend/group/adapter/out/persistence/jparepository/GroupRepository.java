package com.voiceprint.backend.group.adapter.out.persistence.jparepository;

import com.voiceprint.backend.group.adapter.out.persistence.GroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalTime;
import java.util.List;

public interface GroupRepository extends JpaRepository<GroupJpaEntity, Integer> {
    @Query("SELECT g FROM Group g JOIN GroupUser gu ON g.id = gu.group.id WHERE gu.user.id = :userId")
    List<GroupJpaEntity> findAllByUserId(@Param("userId") Integer userId);


    List<GroupJpaEntity> findByAlarmTimeAndEnableAlarmTrue(
            @Param("alarmTime") LocalTime now);

}

