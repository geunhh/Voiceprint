package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // 첫 페이지용
    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.user u
        JOIN FETCH u.profileImage p
        WHERE c.groupDiary.id = :groupDiaryId
        ORDER BY c.id DESC
        """)
    List<Comment> findFirstComment(
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
        ORDER BY c.id DESC
        """)
    List<Comment> findAfterFirstComment(
            @Param("groupDiaryId") long groupDiaryId,
            @Param("cursor") Integer cursor,
            Pageable pageable
    );


}
