package com.voiceprint.backend.api.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CommentGetResponseDTO {
    private long userId;
    private String userName;
    private String userImage;
    private long commentId;
    private LocalDateTime createdAt;
    private String content;
}
