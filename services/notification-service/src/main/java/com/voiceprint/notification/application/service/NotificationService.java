package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.adapter.in.web.dto.NotificationListWithCursorDTO;
import com.voiceprint.notification.adapter.out.AsyncNotificationPublisher;
import com.voiceprint.notification.adapter.out.RedisPublisher;
import com.voiceprint.notification.adapter.out.persistence.*;
import com.voiceprint.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.notification.domain.Notification;
import com.voiceprint.notification.application.port.in.NotificationCommandPort;
import com.voiceprint.notification.application.port.in.NotificationQueryPort;
import com.voiceprint.notification.dto.BatchResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationService implements NotificationCommandPort, NotificationQueryPort {

    private final NotificationRepositoryPort notificationPort;
    private final RedisPublisher redisPublisher;
    private final NotificationMapper notificationMapper;
    private final RedisTemplate<String, Object> redisTemplate;            // Redis 상태 조회용 (세션 상태 등)
    private final NotificationMessageFactory notificationMessageFactory;  // status 기반 알림 메시지 생성
    private final NotificationJdbcRepository notificationJdbcRepository;
    private final AsyncNotificationPublisher asyncNotificationPublisher; // @Async로 Redis publish
    private final NotificationPerfMonitor perfMonitor;                    // 성능 측정용

    private static final String SESSION_KEY_PREFIX = "session";

    @PersistenceContext
    EntityManager em;


    // JDBC 배치 기반 알림 처리.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult processBatchWithJdbc(List<UserNotificationPreferenceJpaEntity> batch) {

        int sent = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        List<NotificationJpaEntity> entities = new ArrayList<>();
        List<NotificationDTO> publishDtos = new ArrayList<>();

        long batchStart = System.currentTimeMillis();

        for (UserNotificationPreferenceJpaEntity user : batch) {
            try {
                Integer userId = user.getUserId();
                String sessionKey = SESSION_KEY_PREFIX + ":" + userId;

                // 1) Redis에서 상태 조회
                long rStart = System.nanoTime();
                String status = "NOT_EXIST";
                Boolean hasStatus = redisTemplate.opsForHash().hasKey(sessionKey, "status");
                if (Boolean.TRUE.equals(hasStatus)) {
                    Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
                    status = statusObj != null ? statusObj.toString().replace("\"", "") : "NOT_EXIST";
                }
                long rEnd = System.nanoTime();
                perfMonitor.addRedisGet(rEnd - rStart);

                // 2) status 기반으로 알림 메시지 생성
                NotificationDTO payload = notificationMessageFactory.createNotification(status);
                if (payload == null) {
                    skipped++;
                    continue;
                }

                // 3) 메타데이터(userId 포함) 구성
                Map<String, Object> meta = new HashMap<>();
                if (payload.getMetadata() != null) {
                    meta.putAll(payload.getMetadata());
                }
                meta.put("userId", userId);   // subscriber가 이걸로 SSE 보냄

                // 4) DB insert용 엔티티 구성
                NotificationJpaEntity entity = NotificationJpaEntity.create(
                        userId,
                        payload.getType(),
                        payload.getMessage(),
                        meta
                );
                entities.add(entity);

                // 5) Redis publish용 DTO도 같이 적재
                NotificationDTO publishDto = new NotificationDTO(
                        payload.getType(),
                        payload.getMessage(),
                        meta
                );
                publishDtos.add(publishDto);

                sent++;

            } catch (Exception e) {
                log.error("Error processing user {} during JDBC batch", user.getUserId(), e);
                errors.add("userId=" + user.getUserId() + ", err=" + e.getMessage());
            }
        }

        // 6) JDBC 배치 INSERT (entities가 있을 때만)
        if (!entities.isEmpty()) {
            long saveStart = System.nanoTime();
            notificationJdbcRepository.saveAllBatch(entities);
            long saveEnd = System.nanoTime();
            perfMonitor.addDbInsert(saveEnd - saveStart);

            // 7) 커밋 후 Async로 Redis publish
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    long pubStart = System.nanoTime();
                    asyncNotificationPublisher.publishAllAsync(publishDtos);
                    long pubEnd = System.nanoTime();
                    perfMonitor.addRedisPublish(pubEnd - pubStart);
                }
            });
        }

        long batchEnd = System.currentTimeMillis();
        log.info("[BATCH][JDBC] size={} took={}ms, sent={}, skipped={}",
                batch.size(), (batchEnd - batchStart), sent, skipped);

        return new BatchResult(sent, skipped, errors);
    }

    // V3 실험 B: Redis GET + DB insert만 (publish 제거) //TODO :테스트용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult processBatchRedisAndDbNoPublish(List<UserNotificationPreferenceJpaEntity> batch) {

        int sent = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (UserNotificationPreferenceJpaEntity user : batch) {
            try {
                Integer userId = user.getUserId();
                String sessionKey = SESSION_KEY_PREFIX + ":" + userId;

                // Redis GET 계측
                long rStart = System.nanoTime();
                String status = "NOT_EXIST";
                Boolean hasStatus = redisTemplate.opsForHash().hasKey(sessionKey, "status");
                if (Boolean.TRUE.equals(hasStatus)) {
                    Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
                    status = statusObj != null ? statusObj.toString().replace("\"", "") : "NOT_EXIST";
                }
                long rEnd = System.nanoTime();
                perfMonitor.addRedisGet(rEnd - rStart);

                NotificationDTO payload = notificationMessageFactory.createNotification(status);
                if (payload == null) {
                    skipped++;
                    continue;
                }

                // DB insert (publish 없음)
                sendAndSaveWithBatchNoPublish(user, payload);
                sent++;

            } catch (Exception e) {
                log.error("Error processing user {} during V3-Redis+DbNoPub", user.getUserId(), e);
                errors.add("userId=" + user.getUserId() + ", err=" + e.getMessage());
            }
        }

        return new BatchResult(sent, skipped, errors);
    }


    // V3 실험 A: Redis GET만 있는 버전 : TODO: 테스트용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult processBatchRedisOnly(List<UserNotificationPreferenceJpaEntity> batch) {

        int sent = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (UserNotificationPreferenceJpaEntity user : batch) {
            try {
                Integer userId = user.getUserId();
                String sessionKey = SESSION_KEY_PREFIX + ":" + userId;

                long rStart = System.nanoTime();

                String status = "NOT_EXIST";
                Boolean hasStatus = redisTemplate.opsForHash().hasKey(sessionKey, "status");
                if (Boolean.TRUE.equals(hasStatus)) {
                    Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
                    status = statusObj != null ? statusObj.toString().replace("\"", "") : "NOT_EXIST";
                }

                long rEnd = System.nanoTime();
                perfMonitor.addRedisGet(rEnd - rStart);

                // DTO 만들긴 하는데 DB 저장은 안 함
                NotificationDTO payload = notificationMessageFactory.createNotification(status);
                if (payload == null) {
                    skipped++;
                    continue;
                }

                // DB insert / publish 없음
                sent++;

            } catch (Exception e) {
                log.error("Error processing user {} during V3-RedisOnly", user.getUserId(), e);
                errors.add("userId=" + user.getUserId() + ", err=" + e.getMessage());
            }
        }

        return new BatchResult(sent, skipped, errors);
    }


    // --- V3용: 1000개 배치 단위로 REQUIRES_NEW ---
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult processBatchWithNewTransaction(List<UserNotificationPreferenceJpaEntity> batch) {

        int sent = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        long batchStart = System.currentTimeMillis();

        for (UserNotificationPreferenceJpaEntity user : batch) {
            try {
                // (1) 여기로 Redis 상태 조회 로직을 옮기거나,
                //     혹은 DTO 를 바깥에서 만들어서 넘겨줘도 됨.
                Integer userId = user.getUserId();
                String sessionKey = SESSION_KEY_PREFIX + ":" + userId;

                long rStart = System.nanoTime();

                String status = "NOT_EXIST";
                Boolean hasStatus = redisTemplate.opsForHash().hasKey(sessionKey, "status");
                if (Boolean.TRUE.equals(hasStatus)) {
                    Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
                    status = statusObj != null ? statusObj.toString().replace("\"", "") : "NOT_EXIST";
                }
                long rEnd = System.nanoTime();
                perfMonitor.addRedisGet(rEnd - rStart);

                NotificationDTO payload = notificationMessageFactory.createNotification(status);
                if (payload == null) {
                    skipped++;
                    continue;
                }

                // (2) 기존의 배치용 메소드 재사용
                sendAndSaveWithBatch(user, payload);
                sent++;

            } catch (Exception e) {
                log.error("Error processing user {} during notification batch", user.getUserId(), e);
                errors.add("userId=" + user.getUserId() + ", err=" + e.getMessage());
            }
        }

        // 이 메소드가 끝날 때 트랜잭션 커밋:
        // - em.flush() 자동 호출
        // - 여기까지 persist 된 알림들이 한 번에 INSERT
        // - 이 트랜잭션에서 registerSynchronization 한 afterCommit 콜백들 실행 → Redis publish

        long batchEnd = System.currentTimeMillis();
        log.info("[BATCH][V3] size={} took={}ms, sent={}, skipped={}",
                batch.size(), (batchEnd - batchStart), sent, skipped);

        return new BatchResult(sent, skipped, errors);
    }

    /**
     * 알림 생성 + DB 저장 + Redis Pub/Sub 전송
     */
    @Override
    public void sendAndSave(UserNotificationPreferenceJpaEntity user, NotificationDTO dto) {
        Notification notification = Notification.create(
                user.getUserId(),
                dto.getType(),
                dto.getMessage(),
                dto.getMetadata()
        );
        Notification savedNotification = notificationPort.save(notification);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NotificationDTO inputDto = new NotificationDTO(
                        dto.getType(),
                        dto.getMessage(),
                        Map.of(
                                "notificationId", savedNotification.getId(),
                                "userId", user.getId()
                        )
                );
                redisPublisher.publishNotification(inputDto);
            }
        });
    }

    // 배치 경계에서 호출할 flush/clear
    public void flushAndClear() {
        em.flush();  // 쌓여있는 INSERT/UPDATE를 DB로 실제 전송
        em.clear();  // 1차 캐시 비우기 (메모리 절약)
    }

    // publish 없는 배치 insert 전용
    public void sendAndSaveWithBatchNoPublish(UserNotificationPreferenceJpaEntity user, NotificationDTO dto) {
        Notification notification = Notification.create(
                user.getUserId(),
                dto.getType(),
                dto.getMessage(),
                dto.getMetadata()
        );
        NotificationJpaEntity entity = notificationMapper.toJpaEntity(notification);

        long saveStart = System.nanoTime();
        em.persist(entity);
        long saveEnd = System.nanoTime();
        perfMonitor.addDbInsert(saveEnd - saveStart);

        // afterCommit 등록 안 함 => Redis publish 없음
    }


    /**
     * 알림 생성 + DB 저장 + Redis Pub/Sub 전송
     *
     * 배치 처리할 경우, 즉시 getId 가 되지 않기에 처리한 로직
     */
    @Override
    public void sendAndSaveWithBatch(UserNotificationPreferenceJpaEntity user, NotificationDTO dto) {
        Notification notification = Notification.create(
                user.getUserId(),
                dto.getType(),
                dto.getMessage(),
                dto.getMetadata()
        );
        NotificationJpaEntity entity = notificationMapper.toJpaEntity(notification);

        // === db insert 측정
        long saveStart = System.nanoTime();
        em.persist(entity); // JPA가 INSERT 쿼리 쌓아둠 (의도)
        long saveEnd = System.nanoTime();
        perfMonitor.addDbInsert(saveEnd - saveStart);

        final Integer userId = user.getUserId();
        final String type = dto.getType();
        final String message = dto.getMessage();
        final Map<String, Object> metadata = dto.getMetadata();


        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Map<String, Object> meta = new HashMap<>();
                if (metadata != null) {
                    meta.putAll(metadata);
                }
//                meta.put("notificationId", entity.getId()); // JPA -> JDBCTemplate 변경 예정
                meta.put("userId", userId);

                NotificationDTO inputDto = new NotificationDTO(
                        type,
                        message,
                        meta
                );
                long pubStart = System.nanoTime();
                redisPublisher.publishNotification(inputDto);
                long pubEnd = System.nanoTime();
                perfMonitor.addRedisPublish(pubEnd - pubStart);
            }
        });
    }

    /**
     * 알림 생성 + DB 저장 + Redis Pub/Sub 전송
     *
     * Transaction 전파를 REQUIRES_NEW로 명시 : 커밋과 publish 단건으로 즉시 처리됨.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAndSaveWithNewTransaction(UserNotificationPreferenceJpaEntity user, NotificationDTO dto) {
        Notification notification = Notification.create(
                user.getUserId(),
                dto.getType(),
                dto.getMessage(),
                dto.getMetadata()
        );

        // DB insert 시간 측정
        long saveStart = System.nanoTime();
        Notification savedNotification = notificationPort.save(notification);
        long saveEnd = System.nanoTime();
        perfMonitor.addDbInsert(saveEnd - saveStart);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                NotificationDTO inputDto = new NotificationDTO(
                        dto.getType(),
                        dto.getMessage(),
                        Map.of(
                                "notificationId", savedNotification.getId(),
                                "userId", user.getId()
                        )
                );

                long pubStart = System.nanoTime();
                redisPublisher.publishNotification(inputDto);
                long pubEnd = System.nanoTime();
                perfMonitor.addRedisPublish(pubEnd - pubStart);
            }
        });
    }


    /**
     * 읽지 않은 알림 정보를 조회하는 메서드 (커서 기반 무한스크롤)
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationListWithCursorDTO getUnreadNotifications(Integer userId, Long cursor, Integer size) {
        List<Notification> notifications = notificationPort.findMyNotifications(userId, cursor, size + 1);

        boolean hasNext = notifications.size() > size;

        if (hasNext) {
            notifications = notifications.subList(0, size);
            log.info("다음 알림 존재");
        } else {
            log.info("마지막 알람입니다.");
        }

        Long nextCursor = hasNext ? notifications.get(notifications.size() - 1).getId() : null;

        List<NotificationDTO> response = notifications.stream()
                .map(n -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("notificationId", n.getId());

                    if (n.getMetadata() != null) {
                        if (n.getMetadata().get("groupId") != null)
                            metadata.put("groupId", n.getMetadata().get("groupId"));
                        if (n.getMetadata().get("diaryId") != null)
                            metadata.put("diaryId", n.getMetadata().get("diaryId"));
                        if (n.getMetadata().get("status") != null)
                            metadata.put("status", n.getMetadata().get("status"));
                    }

                    return new NotificationDTO(n.getType(), n.getMessage(), metadata);
                })
                .collect(Collectors.toList());

        return new NotificationListWithCursorDTO(response, nextCursor);
    }

    /**
     * 알림 읽음 처리 메서드
     */
    @Override
    public void markNotification(Integer userId, Long notificationId) {
        notificationPort.markAsRead(notificationId, userId);
        log.debug("noti Id : {} -  읽음 처리 성공", notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public void publishAllNotifications(List<Notification> notifications) {
        log.debug("알림 전송하기");
        for (Notification n : notifications) {
            log.debug("notification : {}", n.getMessage());
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("notificationId", n.getId());
            metadata.put("groupId", n.getMetadata() != null ? n.getMetadata().getOrDefault("groupId", null) : null);
            metadata.put("diaryId", n.getMetadata() != null ? n.getMetadata().getOrDefault("diaryId", null) : null);

            NotificationDTO dto = new NotificationDTO(
                    n.getType(),
                    n.getMessage(),
                    metadata
            );
            log.debug("notiDTO : {}", dto);
            log.debug("notiDTO meta : {}", dto.getMetadata());
            redisPublisher.publishNotification(dto);
        }
    }

    @Transactional
    public void updateNotificationMetadata(List<Notification> notifications) {
        log.warn("updateNotificationMetadata method needs implementation using NotificationPort.");
    }


}