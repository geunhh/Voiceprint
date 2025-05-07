package com.voiceprint.backend.common.exception.chat;

public class SessionAlreadyExistsException extends RuntimeException{
    public SessionAlreadyExistsException(String message) {
        super(message);
    }

}
