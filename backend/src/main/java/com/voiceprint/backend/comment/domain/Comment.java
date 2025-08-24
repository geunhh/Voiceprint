package com.voiceprint.backend.comment.domain;

import com.voiceprint.backend.domain.Entity.GroupDiary;
import com.voiceprint.backend.domain.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 도메인 엔티티(코어 도메인 계층). 외부 프레임워크 의존성 없이 비즈니스 규칙을 캡슐화합니다.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Comment {

    private final Integer id;
    private final Integer userId;
    private final Integer groupDiaryId;
    private final String content;
    private final boolean isDeleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * 댓글 삭제 처리
     * - 논리적 삭제
     */
    public Comment deleteComment() {
        if (this.isDeleted) {
            return this;
        }
        return this.toBuilder()
                .isDeleted(true)
                .build();
    }
}
