package com.voiceprint.backend.common.exception;

import com.voiceprint.backend.common.exception.chat.ChatSessionNotFoundException;
import com.voiceprint.backend.common.exception.chat.RedisUnavailableException;
import com.voiceprint.backend.common.exception.chat.SessionAlreadyExistsException;
import com.voiceprint.backend.common.exception.diary.DiaryNotFoundException;
import com.voiceprint.backend.common.exception.diary.DiaryThemaNotFoundException;
import com.voiceprint.backend.common.exception.diary.InvalidPromptException;
import com.voiceprint.backend.common.exception.diary.UnauthorizedDiaryAccessException;
import com.voiceprint.backend.common.exception.thema.ThemaNotFoundExceiption;
import com.voiceprint.backend.common.exception.thema.UnauthorizedThemaAccessException;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
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

    @ExceptionHandler(UnauthorizedThemaAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedThema(UnauthorizedThemaAccessException e) {
        return e.getMessage();
    }

    @ExceptionHandler(ThemaNotFoundExceiption.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleThemaNotFound(ThemaNotFoundExceiption e) {
        return e.getMessage();
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(UserNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(DiaryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleDiaryNotFound(DiaryNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(UnauthorizedDiaryAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedThema(UnauthorizedDiaryAccessException e) {
        return e.getMessage();
    }


    @ExceptionHandler(InvalidPromptException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public String handleInvalidPrompt(InvalidPromptException e) {
        return e.getMessage();
    }

    @ExceptionHandler(DiaryThemaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public String handleDiaryThemaNotFound(DiaryThemaNotFoundException e) {
        return e.getMessage();
    }

    // 공통 응답 생성메서드

}
