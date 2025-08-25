package com.voiceprint.backend.comment.adapter.out.persistence;

import com.voiceprint.backend.domain.Entity.GroupDiary;
import com.voiceprint.backend.domain.Entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA 엔티티
 * DB 스키마와 매핑되며, 도메인 객체와는 분리
 */
@Entity(name = "Comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_comment_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_diary_id", foreignKey = @ForeignKey(name = "fk_comment_groupDiary"))
    private GroupDiary groupDiary;

    @Column(name = "content", nullable = false, length = 255)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updated_at", updatable = true)
    private LocalDateTime updatedAt;

    public void deleteComment() {
        if (this.isDeleted) {
            return;
        }
        this.isDeleted = true;
    }

    @Builder
    private CommentEntity(User user,
                          GroupDiary groupDiary,
                          String content,
                          boolean isDeleted,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
        this.user = user;
        this.groupDiary = groupDiary;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
