package com.voiceprint.backend.common.exception.group;

public class AlreadyJoinedGroupException extends RuntimeException {
    public AlreadyJoinedGroupException(String message) {
        super(message);
    }
}
