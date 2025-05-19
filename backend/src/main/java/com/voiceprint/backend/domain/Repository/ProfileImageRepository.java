package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Byte> {
}
