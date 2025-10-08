package com.voiceprint.backend.user.adapter.out.persistence;

import com.voiceprint.backend.user.domain.ProfileImage;
import org.springframework.stereotype.Component;

@Component
class ProfileImageMapper {

    ProfileImage toDomain(ProfileImageJPAEntity entity) {
        return com.voiceprint.backend.user.domain.ProfileImage.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
