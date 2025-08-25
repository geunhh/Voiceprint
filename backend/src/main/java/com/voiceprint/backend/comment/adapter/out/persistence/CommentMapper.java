package com.voiceprint.backend.comment.adapter.out.persistence;

import com.voiceprint.backend.comment.domain.Comment;
import com.voiceprint.backend.domain.Entity.GroupDiary;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * 엔티티 <-> 도메인 매퍼
 */
@Component
public class CommentMapper {

    @PersistenceContext
    private EntityManager em;

    // JPA Entity -> Domain
    public Comment toDomain(CommentEntity e) {
        return Comment.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .groupDiaryId(e.getGroupDiary().getId())
                .content(e.getContent())
                .isDeleted(e.isDeleted())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    // Domain -> JPA Entity
    public CommentEntity toEntity(Comment d) {
        if (d.getUserId() == null || d.getGroupDiaryId() == null) {
            throw new IllegalArgumentException("userId/groupDiaryId는 null일 수 없습니다.");
        }
        UserJPAEntity userRef = em.getReference(UserJPAEntity.class, d.getUserId());
        GroupDiary diaryRef = em.getReference(GroupDiary.class, d.getGroupDiaryId());
        return CommentEntity.builder()
                .user(userRef)
                .groupDiary(diaryRef)
                .content(d.getContent())
                .isDeleted(d.isDeleted())
                .build();
    }
}
