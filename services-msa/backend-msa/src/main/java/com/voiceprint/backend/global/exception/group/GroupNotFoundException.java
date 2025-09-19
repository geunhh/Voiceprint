package com.voiceprint.backend.global.exception.group;

public class GroupNotFoundException extends RuntimeException{
    public GroupNotFoundException(String message) {
        super(message);
    }

}