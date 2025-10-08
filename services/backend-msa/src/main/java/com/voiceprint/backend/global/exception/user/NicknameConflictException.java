package com.voiceprint.backend.global.exception.user;

public class NicknameConflictException extends RuntimeException {
    public NicknameConflictException(String message) {
        super(message);
    }
}
