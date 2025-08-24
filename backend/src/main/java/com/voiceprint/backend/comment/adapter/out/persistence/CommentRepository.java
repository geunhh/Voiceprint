package com.voiceprint.backend.comment.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



import java.util.List;

/**
 * Spring Data JPA Repository
 * - Persistence Adapter 에서 호출하는 구체 구현체.
 */
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    // 첫 페이지용
    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.user u
        JOIN FETCH u.profileImage p
        WHERE c.groupDiary.id = :groupDiaryId
        AND NOT c.isDeleted 
        ORDER BY c.id DESC
        """)
    List<CommentEntity> findFirstComment(
            @Param("groupDiaryId") long groupDiaryId,
            Pageable pageable
    );

    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.user u
        JOIN FETCH u.profileImage p
        WHERE c.groupDiary.id = :groupDiaryId
        AND c.id < :cursor
        AND NOT c.isDeleted
        ORDER BY c.id DESC
        """)
    List<CommentEntity> findAfterFirstComment(
            @Param("groupDiaryId") long groupDiaryId,
            @Param("cursor") Integer cursor,
            Pageable pageable
    );


}
