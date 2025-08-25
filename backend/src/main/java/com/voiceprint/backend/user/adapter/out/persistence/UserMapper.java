package com.voiceprint.backend.user.adapter.out.persistence;

import com.voiceprint.backend.chat.adapter.out.persistence.ChatbotJPAEntity;
import com.voiceprint.backend.chat.adapter.out.persistence.ChatbotRepository;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryThema;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryThemaRepository;
import com.voiceprint.backend.user.domain.AuthProvider;
import com.voiceprint.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class UserMapper {

    private final ProfileImageRepository profileImageRepository;
    private final DiaryThemaRepository diaryThemaRepository;
    private final ChatbotRepository chatbotRepository;

    User toDomain(UserJPAEntity entity) {
        return User.builder()
                .id(entity.getId())
                .providerId(entity.getProviderId())
                .nickname(entity.getNickname())
                .authProvider(AuthProvider.valueOf(entity.getAuthProvider().name()))
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .profileImageId(entity.getProfileImage().getId() != null ? entity.getProfileImage().getId() : null)
                .build();
    }

    UserJPAEntity toEntity(User domain) {
        return UserJPAEntity.builder()
                .id(domain.getId())
                .providerId(domain.getProviderId())
                .nickname(domain.getNickname())
                .authProvider(com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity.AuthProvider.valueOf(domain.getAuthProvider().name()))
                .isDeleted(domain.isDeleted())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .profileImage(domain.getProfileImageId() != null ? profileImageRepository.findById(domain.getProfileImageId()).orElse(null) : null)
                .usingThema(domain.getUsingThemaId() != null ? diaryThemaRepository.findById(domain.getUsingThemaId()).orElse(null) : null)
                .lastChatbot(domain.getLastChatbotId() != null ? chatbotRepository.findById(domain.getLastChatbotId()).orElse(null) : null)
                .build();
    }
}
