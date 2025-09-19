package com.voiceprint.backend.user.application.port.in;

import com.voiceprint.backend.user.adapter.in.web.dto.ProfileImageResponse;
import java.util.List;

public interface GetProfileImagesUseCase {
    List<ProfileImageResponse> getProfileImages();
}
