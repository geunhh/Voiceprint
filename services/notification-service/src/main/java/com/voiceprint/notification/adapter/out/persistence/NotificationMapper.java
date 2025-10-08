package com.voiceprint.notification.adapter.out.persistence;

import com.voiceprint.notification.domain.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toDomain(NotificationJpaEntity entity) {
        return Notification.builder()
                .id(entity.getId())
                .userId(entity.getUserId()) //userId
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


        entity.setUserId(domain.getUserId());
        entity.setType(domain.getType());
        entity.setMessage(domain.getMessage());
        entity.setMetadata(domain.getMetadata());
        entity.setRead(domain.isRead());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }
}