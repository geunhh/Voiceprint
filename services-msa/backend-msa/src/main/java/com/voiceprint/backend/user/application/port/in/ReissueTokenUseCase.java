package com.voiceprint.backend.user.application.port.in;

import com.voiceprint.backend.user.adapter.in.web.dto.TokenResponse;

public interface ReissueTokenUseCase {
    TokenResponse reissueToken(String refreshToken);
}
