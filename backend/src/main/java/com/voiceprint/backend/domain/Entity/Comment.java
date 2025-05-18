package com.voiceprint.backend.domain.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;


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
    private Comment(User user,
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
