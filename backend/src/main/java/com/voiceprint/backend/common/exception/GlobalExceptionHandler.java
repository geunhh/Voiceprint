package com.voiceprint.backend.common.exception;

import com.voiceprint.backend.common.exception.chat.ChatSessionNotFoundException;
import com.voiceprint.backend.common.exception.chat.RedisUnavailableException;
import com.voiceprint.backend.common.exception.chat.SessionAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Spring 전역 예외처리 어노테이션
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public String handleSessionAlreadyExists(SessionAlreadyExistsException e) {
        return e.getMessage();
    }

    @ExceptionHandler(RedisUnavailableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) //500
    public String handleRedisUnavailable(RedisUnavailableException e) {
        return e.getMessage();
    }

    @ExceptionHandler(ChatSessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleSessionNotFound(ChatSessionNotFoundException e) {
        return e.getMessage();
    }

    // 공통 응답 생성메서드

}
