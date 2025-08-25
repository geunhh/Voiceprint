package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.GroupDiary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupDiaryRepository extends JpaRepository<GroupDiary, Integer> {
    // 그룹 ID를 기준으로 최신 순으로 그룹 다이어리 가져오기
    @Query("""
    SELECT gd FROM GroupDiary gd JOIN FETCH gd.diary d WHERE gd.group.id = :groupId
    AND (:cursor IS NULL OR gd.sharedAt < :cursor) ORDER BY gd.sharedAt DESC
    """)
    List<GroupDiary> findGroupDiariesWithCursor(
            @Param("groupId") Integer groupId,
            @Param("cursor") LocalDateTime cursor,
            Pageable pageable
    );


    @Query("""
    SELECT gd FROM GroupDiary gd JOIN FETCH gd.diary d
    WHERE gd.group.id IN :groupIds AND d.user.id != :userId
    AND (:cursor IS NULL OR gd.sharedAt < :cursor)
    ORDER BY gd.sharedAt DESC
    """)
    List<GroupDiary> findByGroupIdsWithCursorExcludeUser(
            @Param("groupIds") List<Integer> groupIds,
            @Param("cursor") LocalDateTime cursor,
            @Param("userId") Integer userId, Pageable pageable);
    Optional<GroupDiary> findByGroupIdAndDiaryId(Integer groupId, Integer diaryId);

    boolean existsByGroupIdAndDiaryId(Integer groupId, Integer diaryId);
}
