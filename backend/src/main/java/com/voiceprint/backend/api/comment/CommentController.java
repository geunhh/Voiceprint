package com.voiceprint.backend.api.comment;

import com.voiceprint.backend.api.comment.dto.CommentCreatRequestDTO;
import com.voiceprint.backend.api.comment.dto.CommentCreateResponseDTO;
import com.voiceprint.backend.api.comment.dto.CommentListWithCursorDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.comment.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/comment")
public class CommentController {
    private final AuthService authService;
    private final CommentService commentService;

    public CommentController(AuthService authService,
                             CommentService commentService) {
        this.authService = authService;
        this.commentService = commentService;
    }


    // 댓글 작성
    @PostMapping("/{groupDiaryId}")
    public ResponseEntity<CommonResponse<CommentCreateResponseDTO>> creatComment (HttpServletRequest request,
                                                                                  @RequestBody CommentCreatRequestDTO commentCreatRequestDTO,
                                                                                  @PathVariable("groupDiaryId") Integer groupDiaryId) {
        // 1. 로그인한 유저 아이디 조회
        Integer userId = authService.getUserIdFromRequest(request);

        // 2. 서비스 호출
        CommentCreateResponseDTO responseDTO = commentService.saveComment(userId, groupDiaryId, commentCreatRequestDTO);

        return ResponseEntity.ok(new CommonResponse<>(201, "댓글 작성 성공", responseDTO));
    }

    // 댓글 조회
    @GetMapping("/{groupDiaryId}")
    public ResponseEntity<CommentListWithCursorDTO> getComment (@PathVariable("groupDiaryId") long groupDiaryId,
                                                                @RequestParam(required = false) Integer cursor,
                                                                @RequestParam(defaultValue = "7") Integer size) {

        CommentListWithCursorDTO result = commentService.getComments(groupDiaryId, cursor, size);
        return ResponseEntity.ok(result);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment (HttpServletRequest request,
                                                            @PathVariable("commentId") Integer commentId) {

        // 1. 로그인한 유저 아이디 조회
        Integer userId = authService.getUserIdFromRequest(request);
        commentService.deleteComment(commentId, userId);
        CommonResponse<Void> commonResponse = new CommonResponse<>(204, "댓글 삭제 성공", null);
        return ResponseEntity.ok(commonResponse);
    }


}
