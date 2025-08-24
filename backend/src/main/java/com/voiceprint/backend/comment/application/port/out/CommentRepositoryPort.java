package com.voiceprint.backend.comment.application.port.out;

import com.voiceprint.backend.comment.domain.Comment;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 출력 포트 인터페이스
 * - 코어에서 외부 영속 계층을 호출할 때 이 인터페이스만 의존.
 */
public interface CommentRepositoryPort {

    Comment save(Comment comment);

    Optional<Comment> findById(Integer commentId);

    List<Comment> findFirstComment(Long groupDiaryId, Pageable pageable);

    List<Comment> findAfterFirstComment(Long groupDiaryId, Integer cursor, Pageable pageable);

    void delete(Comment comment);
}

