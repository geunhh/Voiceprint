package com.voiceprint.backend.global.exception.group;

public class UnauthorizedGroupAccessException extends RuntimeException{
    public UnauthorizedGroupAccessException(String message) {
        super(message);
    }
}
