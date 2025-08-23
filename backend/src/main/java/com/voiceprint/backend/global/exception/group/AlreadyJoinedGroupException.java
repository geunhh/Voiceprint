package com.voiceprint.backend.global.exception.group;

public class AlreadyJoinedGroupException extends RuntimeException {
    public AlreadyJoinedGroupException(String message) {
        super(message);
    }
}
