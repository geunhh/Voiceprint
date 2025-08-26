package com.voiceprint.backend.user.application.port.out;

import com.voiceprint.backend.user.adapter.out.persistence.ProfileImageJPAEntity;
import com.voiceprint.backend.user.domain.ProfileImage;

import java.util.List;
import java.util.Optional;

public interface ProfileImageRepositoryPort {
    Optional<ProfileImage> findById(Byte id);
    List<ProfileImage> findAll();

    // 정통 헥사고날 아키텍처를 따르면 외부 요소를 알면 안 되지만...
    Optional<ProfileImageJPAEntity> findJPAById(Byte id);
}
