package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
