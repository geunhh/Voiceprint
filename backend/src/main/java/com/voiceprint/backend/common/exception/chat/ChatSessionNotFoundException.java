package com.voiceprint.backend.common.exception.chat;

public class ChatSessionNotFoundException extends RuntimeException{
    public ChatSessionNotFoundException(String message) {
        super(message);
    }

}
