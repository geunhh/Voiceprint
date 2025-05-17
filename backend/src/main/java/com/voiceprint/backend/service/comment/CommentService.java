package com.voiceprint.backend.service.comment;

import com.voiceprint.backend.api.comment.dto.CommentCreatRequestDTO;
import com.voiceprint.backend.api.comment.dto.CommentCreateResponseDTO;
import com.voiceprint.backend.api.comment.dto.CommentGetResponseDTO;
import com.voiceprint.backend.api.comment.dto.CommentListWithCursorDTO;
import com.voiceprint.backend.domain.Entity.Comment;
import com.voiceprint.backend.domain.Entity.GroupDiary;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.CommentRepository;
import com.voiceprint.backend.domain.Repository.GroupDiaryRepository;
import com.voiceprint.backend.domain.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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

    // 댓글 작성
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

    // 댓글 조회
    public CommentListWithCursorDTO getComments(long groupDiaryId, Integer cursorId, int limit) {
        // 1. PageRequest 생성
        Pageable page = PageRequest.of(0, limit + 1);

        // 2. cursor 유무에 따라 적절한 Repository 메서드 호출
        List<Comment> comments = (cursorId == null)
                ? commentRepository.findFirstComment(groupDiaryId, page)
                : commentRepository.findAfterFirstComment(groupDiaryId, cursorId, page);

        // 3.limit+1개를 가져왔의니, 진짜 반환할 개수가 limit를 초과하면 다음 페이지 있음
        boolean hasNext = comments.size() > limit;

        // 4. 댓글 리스트 자르기
        List<Comment> slice = hasNext
                ? comments.subList(0,limit)
                : comments;

        // 5. 다음 요청에 쓸 cursorId: 잘라낸 마지막 댓글 id
        Integer nextCursor = hasNext
                ? slice.get(slice.size() - 1).getId()
                : null;
        // 6. entity -> dto 매핑
        List<CommentGetResponseDTO> dtoList = slice.stream()
                .map(c -> new CommentGetResponseDTO(
                        c.getUser().getId(),
                        c.getUser().getNickname(),
                        c.getUser().getProfileImage().getImageUrl(),
                        c.getId(),
                        c.getCreatedAt(),
                        c.getContent()
                ))
                .toList();

        // 7. 최종 응답 객체 생성
        CommentListWithCursorDTO responseDTOS = new CommentListWithCursorDTO();
        responseDTOS.setCode(200);
        responseDTOS.setMessage("댓글 조회 성공");
        responseDTOS.setComments(dtoList);
        responseDTOS.setNextCursor(hasNext ? nextCursor : null);

        return responseDTOS;

    }

}
