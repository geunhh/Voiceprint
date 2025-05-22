package com.voiceprint.backend.common.exception.comment;

public class UnauthorizedCommentAccessException extends RuntimeException{
    public UnauthorizedCommentAccessException(String message) {
        super(message);
    }
}
