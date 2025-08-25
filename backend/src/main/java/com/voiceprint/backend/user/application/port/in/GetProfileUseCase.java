package com.voiceprint.backend.user.application.port.in;

import com.voiceprint.backend.user.adapter.in.web.dto.ProfileResponse;

public interface GetProfileUseCase {
    ProfileResponse getProfile(Integer userId);
}
