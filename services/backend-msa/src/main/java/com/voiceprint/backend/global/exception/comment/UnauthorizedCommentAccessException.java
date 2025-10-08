package com.voiceprint.backend.global.exception.comment;

public class UnauthorizedCommentAccessException extends RuntimeException{
    public UnauthorizedCommentAccessException(String message) {
        super(message);
    }
}
