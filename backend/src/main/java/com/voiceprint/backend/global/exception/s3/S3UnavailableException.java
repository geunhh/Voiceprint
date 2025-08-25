package com.voiceprint.backend.global.exception.s3;

public class S3UnavailableException extends RuntimeException{
    public S3UnavailableException(String message) {
        super(message);
    }

}
