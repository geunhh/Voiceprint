package com.voiceprint.backend.notification.adapter.out.persistence;

import com.voiceprint.backend.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.backend.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = notificationMapper.toJpaEntity(notification);
        NotificationJpaEntity savedEntity = notificationRepository.save(entity);
        return notificationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id)
                .map(notificationMapper::toDomain);
    }

    @Override
    public List<Notification> findMyNotifications(Integer userId, Long cursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<NotificationJpaEntity> entities = notificationRepository.findMyNotifications(userId, cursor, pageable);
        return entities.stream()
                .map(notificationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long notificationId, Integer userId) {
        Optional<NotificationJpaEntity> entityOptional = notificationRepository.findByIdAndUserId(notificationId, userId);
        entityOptional.ifPresent(entity -> {
            entity.markAsRead();
            notificationRepository.save(entity);
        });
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        List<NotificationJpaEntity> entities = notifications.stream()
                .map(notificationMapper::toJpaEntity)
                .collect(Collectors.toList());
        List<NotificationJpaEntity> savedEntities = notificationRepository.saveAll(entities);
        return savedEntities.stream()
                .map(notificationMapper::toDomain)
                .collect(Collectors.toList());
    }
}