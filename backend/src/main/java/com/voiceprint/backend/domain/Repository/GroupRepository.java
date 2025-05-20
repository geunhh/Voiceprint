package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalTime;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    @Query("SELECT g FROM Group g JOIN GroupUser gu ON g.id = gu.group.id WHERE gu.user.id = :userId")
    List<Group> findAllByUserId(@Param("userId") Integer userId);


    List<Group> findByAlarmTimeAndEnableAlarmTrue(
            @Param("alarmTime") LocalTime now);

    boolean existsByGroupIdAndDiaryId(Integer groupId, Integer diaryId);
}

