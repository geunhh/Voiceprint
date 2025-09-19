package com.voiceprint.backend.user.application.port.in;

public interface LogoutUseCase {
    void logout(String refreshToken);
}
