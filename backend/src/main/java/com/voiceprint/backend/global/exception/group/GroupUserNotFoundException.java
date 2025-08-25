package com.voiceprint.backend.global.exception.group;

public class GroupUserNotFoundException extends RuntimeException{
    public GroupUserNotFoundException(String message) {
        super(message);
    }
}
