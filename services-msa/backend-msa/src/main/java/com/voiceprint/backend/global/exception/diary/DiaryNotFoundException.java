package com.voiceprint.backend.global.exception.diary;

public class DiaryNotFoundException extends RuntimeException{
    public DiaryNotFoundException(String message) {
        super(message);
    }

}