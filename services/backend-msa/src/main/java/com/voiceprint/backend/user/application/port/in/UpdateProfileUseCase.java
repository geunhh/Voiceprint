package com.voiceprint.backend.user.application.port.in;

import com.voiceprint.backend.user.adapter.in.web.dto.ProfileUpdateRequest;
import com.voiceprint.backend.user.adapter.in.web.dto.ProfileUpdateResponse;

public interface UpdateProfileUseCase {
    ProfileUpdateResponse updateProfile(Integer userId, ProfileUpdateRequest request);
}
