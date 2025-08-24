package com.voiceprint.backend.diary.adapter.out.persistence;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Integer> {

    @Query("""
             SELECT d from Diary d
             LEFT join fetch d.emotion
             where d.user.id = :userId
             and (:cursor is null or d.id < :cursor)
             order by d.id desc
             """)
    List<DiaryEntity> findMyDiaries(@Param("userId") Integer userId, @Param("cursor") Integer cursor, Pageable pageable);

    @Query("""
            select d from Diary d
            LEFT join fetch d.emotion
            where d.user.id = :userId
            and d.createdAt between :startDate and :endDate
            order by d.createdAt desc
            """)
    List<DiaryEntity> findByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<DiaryEntity> findTop5ByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("""
            select d from Diary d
            join fetch d.emotion
            where d.user.id = :userId and d.createdAt
            between :start and :end
            """)
    List<DiaryEntity> findByUserIdAndCreatedAtBetween(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
        select d from Diary d
        join fetch d.emotion
        join fetch d.user
        where d.id = :diaryId
    """)
    Optional<DiaryEntity> findDetailById(
            @Param("dairyId") Integer diaryId);


    

}
