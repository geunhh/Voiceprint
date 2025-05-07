package com.voiceprint.backend.common.exception.thema;

public class UnauthorizedThemaAccessException extends RuntimeException{
    public UnauthorizedThemaAccessException(String message) {
        super(message);
    }

}
