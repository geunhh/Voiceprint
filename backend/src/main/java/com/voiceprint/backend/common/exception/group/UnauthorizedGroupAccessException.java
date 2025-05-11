package com.voiceprint.backend.common.exception.group;

public class UnauthorizedGroupAccessException extends RuntimeException{
    public UnauthorizedGroupAccessException(String message) {
        super(message);
    }
}
