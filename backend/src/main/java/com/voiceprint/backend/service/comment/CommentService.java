package com.voiceprint.backend.service.comment;

import com.voiceprint.backend.api.comment.dto.CommentCreatRequestDTO;
import com.voiceprint.backend.api.comment.dto.CommentCreateResponseDTO;
import com.voiceprint.backend.domain.Entity.Comment;
import com.voiceprint.backend.domain.Entity.GroupDiary;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.CommentRepository;
import com.voiceprint.backend.domain.Repository.GroupDiaryRepository;
import com.voiceprint.backend.domain.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {
    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private GroupDiaryRepository groupDiaryRepository;

    public CommentService (CommentRepository commentRepository,
                           UserRepository userRepository,
                           GroupDiaryRepository groupDiaryRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.groupDiaryRepository =groupDiaryRepository;
    }

    public CommentCreateResponseDTO saveComment (long userId, long groupDiaryId, CommentCreatRequestDTO commentCreatRequestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User를 찾을 수 없습니다."));

        GroupDiary groupDiary = groupDiaryRepository.getById(groupDiaryId);

        Comment comment = Comment.builder()
                .user(user)
                .groupDiary(groupDiary)
                .content(commentCreatRequestDTO.getContent())
                .isDeleted(false)
                .build();

        commentRepository.save(comment);

        CommentCreateResponseDTO responseDTO = new CommentCreateResponseDTO();
        responseDTO.setContent(comment.getContent());
        return responseDTO;
    }
}
