package com.voiceprint.backend.api.comment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommentListWithCursorDTO {
    private int code;
    private String message;
    private List<CommentGetResponseDTO> comments;
    private Integer nextCursor;
}
