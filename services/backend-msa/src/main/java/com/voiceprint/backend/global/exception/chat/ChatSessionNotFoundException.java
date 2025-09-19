package com.voiceprint.backend.global.exception.chat;

public class ChatSessionNotFoundException extends RuntimeException{
    public ChatSessionNotFoundException(String message) {
        super(message);
    }

}
