package com.voiceprint.backend.domain.diary;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("SELECT d from Diary d where d.user.id = :userId" +
            " and (:cursor is null or d.id < :cursor) order by d.id desc")
    List<Diary> findMyDiaries(@Param("userId") Long userId,@Param("cursor") Long cursor, Pageable pageable);
}
