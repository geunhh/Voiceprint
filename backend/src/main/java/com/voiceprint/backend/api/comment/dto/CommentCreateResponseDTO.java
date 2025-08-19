package com.voiceprint.backend.api.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommentCreateResponseDTO {
    private String content;
}
