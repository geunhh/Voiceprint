package com.voiceprint.backend.comment.adapter.out.persistence;

import com.voiceprint.backend.comment.application.port.out.CommentRepositoryPort;
import com.voiceprint.backend.comment.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 영속성 어댑터
 * -
 */
@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentRepositoryPort {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public Comment save(Comment comment) {
        CommentEntity entity = commentMapper.toEntity(comment);
        CommentEntity savedEntity = commentRepository.save(entity);
        return commentMapper.toDomain(savedEntity);
    }

    @Override
    public void update(Comment comment) {

    }

    @Override
    public Optional<Comment> findById(Integer commentId) {
        return commentRepository.findById(commentId).map(commentMapper::toDomain);
    }

    @Override
    public List<Comment> findFirstComment(Long groupDiaryId, Pageable pageable) {
        return commentRepository.findFirstComment(groupDiaryId, pageable)
                .stream()
                .map(commentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Comment> findAfterFirstComment(Long groupDiaryId, Integer cursor, Pageable pageable) {
        return commentRepository.findAfterFirstComment(groupDiaryId, cursor, pageable)
                .stream()
                .map(commentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Comment comment) {
        CommentEntity entity = commentMapper.toEntity(comment);
        commentRepository.delete(entity);
    }
}
