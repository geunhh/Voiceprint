package com.voiceprint.backend.global.exception.thema;

public class UnauthorizedThemaAccessException extends RuntimeException{
    public UnauthorizedThemaAccessException(String message) {
        super(message);
    }

}
