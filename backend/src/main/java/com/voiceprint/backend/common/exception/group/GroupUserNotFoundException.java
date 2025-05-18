package com.voiceprint.backend.common.exception.group;

public class GroupUserNotFoundException extends RuntimeException {
    public GroupUserNotFoundException(String message) {
        super(message);
    }
}
