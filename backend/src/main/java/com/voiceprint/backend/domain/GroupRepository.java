package com.voiceprint.backend.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("SELECT g FROM Group g JOIN GroupUser gu ON g.id = gu.group.id WHERE gu.user.id = :userId")
    List<Group> findAllByUserId(@Param("userId") Long userId);
}

