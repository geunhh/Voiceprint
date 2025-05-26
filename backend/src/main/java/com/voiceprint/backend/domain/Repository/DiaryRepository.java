package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Diary;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> {

    /**
     * 사용자의 작성 일기를 최신순으로 조회하며,
     * 커서 기반 무한 스크롤을 위해 ID < cursor 조건 사용
     * - 감정(emotion) 정보를 fetch join으로 미리 로딩
     */
    @Query("""
             SELECT d from Diary d
             LEFT join fetch d.emotion
             where d.user.id = :userId
             and (:cursor is null or d.id < :cursor)
             order by d.id desc
             """)
    List<Diary> findMyDiaries(@Param("userId") Integer userId,@Param("cursor") Integer cursor, Pageable pageable);

    /**
     * 사용자의 특정 기간 동안 작성한 일기들을 조회합니다.
     * - 감정 정보(emotion)를 fetch join으로 함께 로딩
     */
    @Query("""
            select d from Diary d
            LEFT join fetch d.emotion
            where d.user.id = :userId
            and d.createdAt between :startDate and :endDate
            order by d.createdAt desc
            """)
    List<Diary> findByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 최신 5개의 일기를 조회합니다.
     */
    List<Diary> findTop5ByUserIdOrderByCreatedAtDesc(Integer userId);

    /**
     * 사용자의 특정 기간 동안 작성한 일기들을 조회합니다.
     * - 감정 정보(emotion)를 fetch join
     */
    @Query("""
            select d from Diary d
            join fetch d.emotion
            where d.user.id = :userId and d.createdAt
            between :start and :end
            """)
    List<Diary> findByUserIdAndCreatedAtBetween(
            @Param("userId") Integer userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 특정 일기의 상세 정보를 조회합니다.
     * - 감정 정보(emotion) 및 작성자(user)를 fetch join으로 함께 조회
     */
    @Query("""
        select d from Diary d
        join fetch d.emotion
        join fetch d.user
        where d.id = :diaryId
    """)
    Optional<Diary> findDetailById(
            @Param("dairyId") Integer diaryId);
}
