package com.voiceprint.backend.user.adapter.out.persistence;

import com.voiceprint.backend.user.application.port.out.ProfileImageRepositoryPort;
import com.voiceprint.backend.user.domain.ProfileImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfileImagePersistenceAdapter implements ProfileImageRepositoryPort {

    private final ProfileImageRepository profileImageRepository;
    private final ProfileImageMapper profileImageMapper;

    @Override
    public Optional<ProfileImage> findById(Byte id) {
        return profileImageRepository.findById(id).
                map(profileImageMapper::toDomain);
    }

    @Override
    public List<ProfileImage> findAll() {
        return profileImageRepository.findAll().
                stream().map(profileImageMapper::toDomain)
                .toList();
    }
}
