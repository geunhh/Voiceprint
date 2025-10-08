package com.voiceprint.backend.global.exception.diary;

public class InvalidPromptException extends RuntimeException{
    public InvalidPromptException(String message) {
        super(message);
    }

}