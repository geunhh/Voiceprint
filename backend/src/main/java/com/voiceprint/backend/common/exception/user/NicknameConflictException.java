package com.voiceprint.backend.common.exception.user;

public class NicknameConflictException extends RuntimeException {
    public NicknameConflictException(String message) {
        super(message);
    }
}
