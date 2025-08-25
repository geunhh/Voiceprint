package com.voiceprint.backend.global.exception.diary;

public class UnauthorizedDiaryAccessException extends RuntimeException{
    public UnauthorizedDiaryAccessException(String message) {
        super(message);
    }

}