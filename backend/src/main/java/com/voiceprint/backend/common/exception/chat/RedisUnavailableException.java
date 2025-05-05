package com.voiceprint.backend.common.exception.chat;

public class RedisUnavailableException extends RuntimeException{
    public RedisUnavailableException(String message) {
        super(message);
    }

}
