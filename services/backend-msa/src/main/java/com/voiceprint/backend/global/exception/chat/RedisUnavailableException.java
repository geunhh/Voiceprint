package com.voiceprint.backend.global.exception.chat;

public class RedisUnavailableException extends RuntimeException{
    public RedisUnavailableException(String message) {
        super(message);
    }

}
