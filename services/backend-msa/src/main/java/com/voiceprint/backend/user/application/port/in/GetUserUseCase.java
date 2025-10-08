package com.voiceprint.backend.user.application.port.in;

import jakarta.servlet.http.HttpServletRequest;

public interface GetUserUseCase {
    Integer getUserIdFromRequest(HttpServletRequest request);
    Integer getUserIdFromAuthHeader(String authorizationHeader);
    Integer getUserIdFromToken(String token);
    Integer getUserIdByProviderId(String providerId);
}
