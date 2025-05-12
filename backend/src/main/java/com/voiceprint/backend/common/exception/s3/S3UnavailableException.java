package com.voiceprint.backend.common.exception.s3;

public class S3UnavailableException extends RuntimeException{
    public S3UnavailableException(String message) {
        super(message);
    }

}
