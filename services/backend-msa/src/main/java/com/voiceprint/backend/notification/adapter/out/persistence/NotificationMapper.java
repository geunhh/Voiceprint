package com.voiceprint.backend.notification.adapter.out.persistence;

import com.voiceprint.backend.notification.domain.Notification;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final UserRepository userRepository;

    public Notification toDomain(NotificationJpaEntity entity) {
        return Notification.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .type(entity.getType())
                .message(entity.getMessage())
                .metadata(entity.getMetadata())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public NotificationJpaEntity toJpaEntity(Notification domain) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(domain.getId());

        UserJPAEntity userJpaEntity = null;
        if (domain.getUserId() != null) {
            userJpaEntity = userRepository.findById(domain.getUserId())
                                          .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + domain.getUserId()));
        }
        entity.setUser(userJpaEntity);
        entity.setType(domain.getType());
        entity.setMessage(domain.getMessage());
        entity.setMetadata(domain.getMetadata());
        entity.setRead(domain.isRead());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }
}