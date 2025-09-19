package com.voiceprint.backend.global.exception.chat;

public class SessionAlreadyExistsException extends RuntimeException{
    public SessionAlreadyExistsException(String message) {
        super(message);
    }

}
