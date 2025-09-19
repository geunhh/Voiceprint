package com.voiceprint.backend.user.application.port.in;

public interface GetUserIdFromAuthHeaderUseCase {
    Integer getUserIdFromAuthHeader(String authorizationHeader);
}