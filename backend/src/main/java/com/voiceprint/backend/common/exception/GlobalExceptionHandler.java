package com.voiceprint.backend.common.exception;

import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.common.exception.chat.ChatSessionNotFoundException;
import com.voiceprint.backend.common.exception.chat.RedisUnavailableException;
import com.voiceprint.backend.common.exception.chat.SessionAlreadyExistsException;
import com.voiceprint.backend.common.exception.comment.CommentNotFoundException;
import com.voiceprint.backend.common.exception.comment.UnauthorizedCommentAccessException;
import com.voiceprint.backend.common.exception.diary.*;
import com.voiceprint.backend.common.exception.group.*;
import com.voiceprint.backend.common.exception.s3.InvalidFileException;
import com.voiceprint.backend.common.exception.s3.S3UnavailableException;
import com.voiceprint.backend.common.exception.thema.ThemaNotFoundExceiption;
import com.voiceprint.backend.common.exception.thema.UnauthorizedThemaAccessException;
import com.voiceprint.backend.common.exception.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

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

    @ExceptionHandler(NicknameConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflictException(NicknameConflictException e) {
        return e.getMessage();
    }

    @ExceptionHandler(ProfileImageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleProfileImageNotFound(ProfileImageNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(InvalidFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public String handleInvalidFile(InvalidFileException e) {
        return e.getMessage();
    }

    @ExceptionHandler(S3UnavailableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) //500
    public String handleS3Unavailable(S3UnavailableException e) {
        return e.getMessage();
    }

    @ExceptionHandler(UnauthorizedGroupAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedGroup(UnauthorizedGroupAccessException e) {
        return e.getMessage();
    }

    @ExceptionHandler(ExpiredJwtTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
    public String handleExpiredJwtToken(ExpiredJwtTokenException e) {
        return e.getMessage();
    }

    @ExceptionHandler(UnauthorizedDiaryException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedDiary(UnauthorizedDiaryException e) {
        return e.getMessage();
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotificationNotFound(NotificationNotFoundException e) {
        return e.getMessage();
    }


    @ExceptionHandler(UnauthorizedNotificationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedNotification(UnauthorizedNotificationException e) {
        return e.getMessage();
    }

    @ExceptionHandler(GroupNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleGroupNotFound(GroupNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(GroupUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleGroupUserNotFound(GroupUserNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(InviteNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleInviteNotFound(InviteNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CommonResponse<>(404,e.getMessage(),null));
    }

    @ExceptionHandler(InviteExpiredException.class)
    public ResponseEntity<CommonResponse<Void>> handleInviteExpired(InviteExpiredException e) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new CommonResponse<>(410, e.getMessage(), null));
    }

    @ExceptionHandler(AlreadyJoinedGroupException.class)
    public ResponseEntity<CommonResponse<Void>> handleAlreadyJoined(AlreadyJoinedGroupException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CommonResponse<>(409, e.getMessage(), null));
    }


    @ExceptionHandler(UnauthorizedCommentAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedComment(UnauthorizedCommentAccessException e) { return e.getMessage();}

    @ExceptionHandler(CommentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCommentNotFoundException(CommentNotFoundException e) { return e.getMessage();}
    // 공통 응답 생성메서드

}
