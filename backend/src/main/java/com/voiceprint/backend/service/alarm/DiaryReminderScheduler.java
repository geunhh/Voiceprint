package com.voiceprint.backend.service.alarm;

import com.voiceprint.backend.notification.adapter.in.web.NotificationDTO;
import com.voiceprint.backend.domain.Entity.Group;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.GroupRepository;
import com.voiceprint.backend.domain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiaryReminderScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final GroupRepository groupRepository;

    @Value("${session.key}")
    private String session_key;

    @Scheduled(cron = "0 * * * * *") // 1분 마다 실행
    public void checkAndNotify() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0); // '초'단위 이하 제거.
        int minute = now.getMinute();

        // 0 or 30분일 때만 실행.
//        if (minute != 0 && minute !=30) return;

//        log.info("🕒 리마인더 스케줄러 실행 시작: {}", now);

        notifyIndividualUsers(now);
        notifyGroupUsers(now);
    }

    private void notifyGroupUsers(LocalTime now) {
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();

        // 그룹 리포지토리로부터 해당 시간에 알람 설정된 그룹들 가져오기
        List<Group> candidateGroups = groupRepository.findByAlarmTimeAndEnableAlarmTrue(now);

        // 요일 조건 필터링
        List<Group> targetGroups = candidateGroups.stream()
                .filter(g -> g.getAlarmDays() != null && g.getAlarmDays().contains(today))
                .toList();

        for (Group group : targetGroups) {
            List<User> members = userRepository.findAlarmEnabledUsersByGroup(group); // 알람 켠 멤버만

            for (User user : members) {
                NotificationDTO dto = new NotificationDTO(
                        "groupReminder",
                        String.format("%s 그룹의 공유 시간이 다가왔어요!! 일기를 공유하고 친구들과 일상을 공유하세요!", group.getName()),
                        Map.of(
                                "groupId", group.getId()
                        )
                );

                try {
                    notificationService.sendAndSave(user, dto);
                    log.info("[그룹 알림 전송] userId={}, groupId={}", user.getId(), group.getId());
                } catch (Exception e) {
                    log.error("[그룹 알림 실패] userId={}, groupId={}, err={}", user.getId(), group.getId(), e.getMessage());
                }
            }
        }
    }

    private void notifyIndividualUsers(LocalTime now) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
//                log.debug("userId : {}", user.getId());
                // null 체크 유의.
                if (user.getAlarmTime() == null ||
                        !Boolean.TRUE.equals(user.getEnableAlarm()) ||
                        !user.getAlarmTime().equals(now)) {
//                    log.debug("패스");
                    continue;  // 알람 시간 없음, 알람 꺼짐, 알람 시각 아님 → 패스
                }

                Integer userId = user.getId();
                String sessionKey = session_key + ":" + userId;

                String status = "NOT_EXIST";

                Boolean hasStatus = redisTemplate.opsForHash().hasKey(sessionKey, "status");

                if (hasStatus != null || hasStatus) {
                    Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
                    status = (statusObj != null) ? statusObj.toString().replaceAll("\"", "") : "NOT_EXIST";                    log.debug("status : {}",status);
                }

                NotificationDTO payload = switch (status) {
                    case "WAITING", "NOT_EXIST" -> new NotificationDTO(
                            "reminder",
                            "오늘은 일기 쓰셨나요? 말자국으로 오늘 하루를 기록해 보세요!!",
                            Map.of("status", status)
                    );
                    case "IN_PROGRESS" -> new NotificationDTO(
                            "reminder",
                            "대화가 아직 진행중이에요. 대화를 완료하고 일기를 생성해주세요!",
                            Map.of("status", status)
                    );
                    case "DIARY_DONE", "DIARY_CREATING" -> new NotificationDTO(
                            "reminder", "생성된 일기가 있어요. 저장을 잊지 마세요!",
                            Map.of("status", status)
                    );
                    case "ERROR" -> new NotificationDTO(
                            "reminder", "일기 저장 중 오류가 발생했어요!",
                            Map.of("status", status)
                    );
                    default -> null; // DIARY_SAVED 알림 없음
                };
                log.debug("payload : {}",payload);


                if (payload != null) {
                    try {
                        notificationService.sendAndSave(user, payload);

                        log.info("[알림 저장 및 전송 완료] userId={}, status={}", userId, status);
                    } catch (Exception e) {
                        log.error("[알림 전송 실패] userId={}, error={}", userId, e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.debug("error : {}" , e.getMessage());
            }

        }
    }
}
