package com.voiceprint.backend.user.adapter.out.persistence;

import com.voiceprint.backend.chat.adapter.out.persistence.ChatbotRepository;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryThemaMapper;
import com.voiceprint.backend.user.domain.AuthProvider;
import com.voiceprint.backend.user.domain.User;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryThemaRepository;

@Component
//@RequiredArgsConstructor
public class UserMapper {

    private final ProfileImageRepository profileImageRepository;
    private final ChatbotRepository chatbotRepository;
    private final DiaryThemaMapper diaryThemaMapper;
    private final DiaryThemaRepository diaryThemaRepository;

    public UserMapper(ProfileImageRepository profileImageRepository, ChatbotRepository chatbotRepository,
                      @Lazy DiaryThemaMapper diaryThemaMapper, DiaryThemaRepository diaryThemaRepository) {
        this.profileImageRepository = profileImageRepository;
        this.chatbotRepository = chatbotRepository;
        this.diaryThemaMapper = diaryThemaMapper;
        this.diaryThemaRepository = diaryThemaRepository;
    }

    public User toDomain(UserJPAEntity entity) {
        return User.builder()
                .id(entity.getId())
                .providerId(entity.getProviderId())
                .nickname(entity.getNickname())
                .authProvider(AuthProvider.valueOf(entity.getAuthProvider().name()))
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .profileImageId(entity.getProfileImage() != null ? entity.getProfileImage().getId() : null)
                .usingThema(entity.getUsingThema() != null ? diaryThemaMapper.toDomain(entity.getUsingThema()) : null)
                .customThema(entity.getCustomThema() != null ? diaryThemaMapper.toDomain(entity.getCustomThema()) : null)
                .lastChatbotId(entity.getLastChatbot() != null ? entity.getLastChatbot().getId() : null)
                .enableAlarm(entity.getEnableAlarm())
                .alarmTime(entity.getAlarmTime())
                .build();
    }

    public User toDomainSlim(UserJPAEntity entity) {
        return User.builder()
            .id(entity.getId())
            .providerId(entity.getProviderId())
            .nickname(entity.getNickname())
            .authProvider(AuthProvider.valueOf(entity.getAuthProvider().name()))
            .isDeleted(entity.getIsDeleted())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .profileImageId(entity.getProfileImage() != null ? entity.getProfileImage().getId() : null)
            .lastChatbotId(entity.getLastChatbot() != null ? entity.getLastChatbot().getId() : null)
            .enableAlarm(entity.getEnableAlarm())
            .alarmTime(entity.getAlarmTime())
            .build();
    }

    public UserJPAEntity toEntity(User domain) {
        return UserJPAEntity.builder()
                .id(domain.getId())
                .providerId(domain.getProviderId())
                .nickname(domain.getNickname())
                .authProvider(com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity.AuthProvider.valueOf(domain.getAuthProvider().name()))
                .isDeleted(domain.isDeleted())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .profileImage(domain.getProfileImageId() != null ? profileImageRepository.findById(domain.getProfileImageId()).orElse(null) : null)
                .usingThema(domain.getUsingThema() != null ? diaryThemaRepository.findById(domain.getUsingThema().getId()).orElse(null) : null)
                .customThema(domain.getCustomThema() != null ? diaryThemaMapper.toEntity(domain.getCustomThema()) : null)
                .lastChatbot(domain.getLastChatbotId() != null ? chatbotRepository.findById(domain.getLastChatbotId()).orElse(null) : null)
                .enableAlarm(domain.getEnableAlarm())
                .alarmTime(domain.getAlarmTime())
                .build();
    }
}
