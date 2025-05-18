package com.voiceprint.backend.common.exception.group;

public class GroupNotFoundException extends RuntimeException{
    public GroupNotFoundException(String message) {
        super(message);
    }

}