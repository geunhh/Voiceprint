package com.voiceprint.backend.common.exception.s3;

public class InvalidFileException extends RuntimeException{
    public InvalidFileException(String message) {
        super(message);
    }
}
