package com.voiceprint.backend.comment.application.port.in;

import com.voiceprint.backend.comment.adapter.in.web.dto.CommentCreatRequestDTO;
import com.voiceprint.backend.comment.adapter.in.web.dto.CommentCreateResponseDTO;
import com.voiceprint.backend.comment.adapter.in.web.dto.CommentListWithCursorDTO;


/**
 * 입력 포트
 * Inbound Adapter(controller)가 호출하는 진입점.
 */
public interface CommentUseCase {
    public CommentCreateResponseDTO saveComment (Integer userId, Integer groupDiaryId, CommentCreatRequestDTO commentCreatRequestDTO);

    public CommentListWithCursorDTO getComments(long groupDiaryId, Integer cursorId, int limit);

    public void deleteComment (Integer commentId, Integer userId);
}
