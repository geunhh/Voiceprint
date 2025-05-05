package com.voiceprint.backend.common.exception.diary;

public class UnauthorizedDiaryAccessException extends RuntimeException{
    public UnauthorizedDiaryAccessException(String message) {
        super(message);
    }

}