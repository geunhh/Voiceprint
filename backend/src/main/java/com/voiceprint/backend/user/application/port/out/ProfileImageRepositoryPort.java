package com.voiceprint.backend.user.application.port.out;

import com.voiceprint.backend.user.domain.ProfileImage;

import java.util.List;
import java.util.Optional;

public interface ProfileImageRepositoryPort {
    Optional<ProfileImage> findById(Byte id);
    List<ProfileImage> findAll();
}
