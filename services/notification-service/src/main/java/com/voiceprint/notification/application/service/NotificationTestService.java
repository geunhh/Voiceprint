package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceJpaEntity;
import com.voiceprint.notification.adapter.out.persistence.UserNotificationPreferenceRepository;
import com.voiceprint.notification.dto.BatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 알림 발송 서비스
 *
 * 특정 시간대(LocalTime)에 알림을 수동으로 생성·발송하기 위한 유틸성 서비스 클래스.
 * 운영 스케줄러(DiaryReminderScheduler 등)와 동일한 로직을 사용하지만,
 * 수동으로 특정 시간대나 특정 유저만 대상으로 테스트할 수 있도록 분리함.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationTestService {

    private final UserNotificationPreferenceRepository prefRepo;
    private final NotificationService notificationService;                // 실제 알림 저장 및 전송 처리
    private final NotificationMessageFactory notificationMessageFactory;  // status 기반 알림 메시지 생성
    private final RedisTemplate<String, Object> redisTemplate;            // Redis 상태 조회용 (세션 상태 등)

    private static final String SESSION_KEY_PREFIX = "session";
    private static final int BATCH_SIZE = 1000;

    @Transactional(readOnly = false)
    public NotifyTestResult triggerV3(LocalTime testTime, Integer limit, List<Integer> onlyUserIds) {

        List<UserNotificationPreferenceJpaEntity> targets;

        if (onlyUserIds != null && !onlyUserIds.isEmpty()) {
            targets = prefRepo.findByEnableAlarmsTrueAndAlarmTimeAndUserIdIn(testTime, onlyUserIds);
        } else {
            targets = prefRepo.findByEnableAlarmsTrueAndAlarmTime(testTime);
        }

        if (limit != null && limit > 0 && targets.size() > limit) {
            targets = targets.subList(0, limit);
        }

        int total = targets.size();
        int sent = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        // === 1000개씩 끊어서 Batch 처리 ===
        for (int i = 0; i < targets.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, targets.size());
            List<UserNotificationPreferenceJpaEntity> batch = targets.subList(i, end);

            try {
                BatchResult r = notificationService.processBatchWithNewTransaction(batch);
                sent += r.getSent();
                skipped += r.getSkipped();
                errors.addAll(r.getErrors());
            } catch (Exception e) {
                // 이 배치(1000개) 전체가 롤백된 경우
                errors.add("batch[" + i + "~" + (end - 1) + "] failed : " + e.getMessage());
                log.error("Error processing batch {}-{}", i, end - 1, e);
            }
        }

        // 마지막 잔여분 처리
        notificationService.flushAndClear();

        return new NotifyTestResult(testTime, total, sent, skipped, errors);
    }
    /**
     * v2 – JPA Batch 기반 처리 버전
     *
     * - sendAndSaveWithJpaBatch() 에서 em.persist() + afterCommit 등록만 수행
     * - 여기서 BATCH_SIZE마다 flushAndClear() 호출
     */
    @Transactional(readOnly = false)
    public NotifyTestResult triggerV2(LocalTime testTime, Integer limit, List<Integer> onlyUserIds) {

        List<UserNotificationPreferenceJpaEntity> targets;

        if (onlyUserIds != null && !onlyUserIds.isEmpty()) {
            targets = prefRepo.findByEnableAlarmsTrueAndAlarmTimeAndUserIdIn(testTime, onlyUserIds);
        } else {
            targets = prefRepo.findByEnableAlarmsTrueAndAlarmTime(testTime);
        }

        if (limit != null && limit > 0 && targets.size() > limit) {
            targets = targets.subList(0, limit);
        }

        int total = targets.size();
        int sent = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        int processed = 0;

        for (UserNotificationPreferenceJpaEntity user : targets) {
            try {
                Integer userId = user.getUserId();
                String sessionKey = SESSION_KEY_PREFIX + ":" + userId;

                String status = "NOT_EXIST";
                Boolean hasStatus = redisTemplate.opsForHash().hasKey(sessionKey, "status");

                if (Boolean.TRUE.equals(hasStatus)) {
                    Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
                    status = statusObj != null ? statusObj.toString().replace("\"", "") : "NOT_EXIST";
                }

                NotificationDTO payload = notificationMessageFactory.createNotification(status);

                if (payload == null) {
                    skipped++;
                    continue;
                }

                // JPA Batch 버전: persist만 쌓아두고, afterCommit에서 Redis 발행
                notificationService.sendAndSaveWithBatch(user, payload);
                sent++;
                processed++;

                // 배치 경계마다 flush + clear
                if (processed % BATCH_SIZE == 0) {
                    notificationService.flushAndClear();
                }

            } catch (Exception e) {
                log.error("Error processing user {} during notification test", user.getUserId(), e);
                errors.add("userId=" + user.getUserId() + ", err=" + e.getMessage());
            }
        }

        // 마지막 잔여분 처리
        notificationService.flushAndClear();

        return new NotifyTestResult(testTime, total, sent, skipped, errors);
    }

    /**
     * 테스트 트리거 메서드
     *
     * 주어진 시간대(testTime)에 알림을 발송할 유저를 DB에서 조회한 뒤,
     * 각 유저의 Redis 상태를 확인하고 알림을 생성(sendAndSave)한다.
     *
     * @param testTime     테스트할 알람 시각 (예: 06:30)
     * @param limit        최대 발송 대상 수 (optional)
     * @param onlyUserIds  특정 유저 ID 목록 (optional)
     * @return NotifyTestResult (테스트 결과 요약)
     */
    @Transactional(readOnly = false)
    public NotifyTestResult triggerV1(LocalTime testTime, Integer limit, List<Integer> onlyUserIds) {

        List<UserNotificationPreferenceJpaEntity> targets;

        // 🎯 특정 유저 리스트가 주어졌다면, 해당 유저들만 대상으로 필터링
        if (onlyUserIds != null && !onlyUserIds.isEmpty()) {
            targets = prefRepo.findByEnableAlarmsTrueAndAlarmTimeAndUserIdIn(testTime, onlyUserIds);
        }
        // 🕒 아니면 지정된 시간대(alarmTime) 기준으로 전체 조회
        else {
//            targets = prefRepo.findByEnableAlarmsTrueAndAlarmTime(testTime);
            targets = prefRepo.findByEnableAlarmsTrue();
        }

        // ⚙️ limit 지정 시 상한선 적용
        if (limit != null && limit > 0 && targets.size() > limit) {
            targets = targets.subList(0, limit);
        }

        int total = targets.size(); // 조회된 유저 수
        int sent = 0;               // 실제 발송 완료 수
        int skipped = 0;            // payload 생성 실패 등으로 건너뛴 수
        List<String> errors = new ArrayList<>(); // 개별 예외 로그 수집

        // 📦 각 유저별 알림 처리
        for (UserNotificationPreferenceJpaEntity user : targets) {
            try {
                Integer userId = user.getUserId();
                String sessionKey = SESSION_KEY_PREFIX + ":" + userId;

                // Redis에서 세션 상태 확인
                String status = "NOT_EXIST";
                Boolean hasStatus = redisTemplate.opsForHash().hasKey(sessionKey, "status");

                if (Boolean.TRUE.equals(hasStatus)) {
                    Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
                    status = statusObj != null ? statusObj.toString().replace("\"", "") : "NOT_EXIST";
                }

                // Redis 상태(status)에 따라 알림 메시지 생성
                NotificationDTO payload = notificationMessageFactory.createNotification(status);

                // payload 생성 실패 시 스킵
                if (payload == null) {
                    skipped++;
                    continue;
                }

                // 실제 알림 저장 및 전송
//                notificationService.sendAndSaveWithNewTransaction(user, payload);
                notificationService.sendAndSave(user, payload);
                sent++; //

            } catch (Exception e) {
                // 예외 발생 시 개별 유저 단위로만 로깅 및 계속 진행
                errors.add("userId=" + user.getUserId() + ", err=" + e.getMessage());
                log.error("error: {}",e.getMessage());
            }
        }

        // 🧾 실행 결과 요약 반환
        return new NotifyTestResult(testTime, total, sent, skipped, errors);
    }

    /**
     * 📊 테스트 결과 요약 DTO
     * - 요청 시각
     * - 전체 후보 수
     * - 성공/스킵 수
     * - 에러 목록
     */
    public record NotifyTestResult(
            LocalTime requestedTime,
            int totalCandidates,
            int sent,
            int skipped,
            List<String> errors
    ) {}
}
