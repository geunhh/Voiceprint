package com.voiceprint.backend.api.alarm;

import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.api.alarm.dto.NotificationListWithCursorDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.alarm.NotificationService;
import com.voiceprint.backend.service.alarm.SseService;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notifications")
public class NotificationController {
    private final SseService sseService;
    private final AuthService authService;
    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);

        return sseService.subscribe(userId);
    }

    /**
     * 알림 테스트용 API
     */
    @GetMapping(value = "/subscribe/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testSubscribe() {
        Integer testUserId = 1; // 임시 유저
        return sseService.subscribe(testUserId);
    }

    /**
     * 알림 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<CommonResponse<NotificationListWithCursorDTO>> getUnreadNotifications(
            HttpServletRequest request,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        log.info("userid : {}, 알림 조회 ",userId);
        NotificationListWithCursorDTO response = notificationService.getUnreadNotifications(userId,cursor, size);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "알림 조회 성공", response
        ));
    }

    /**
     * 미조회 알림 개수 API
     */
    @GetMapping("/unread/count")
    public ResponseEntity<CommonResponse<?>> getUnreadCount(
            HttpServletRequest request
    ) {
//        Integer userId = authService.getUserIdFromRequest(request);
        Integer userId = 1;
        Long count = notificationService.getUnreadNotificationCount(userId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "미확인 알림 개수 조회 성공", count
        ));
    }

    /**
     * 알림을 읽음 처리하는 API
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<CommonResponse<Void>> readNotification(
            HttpServletRequest request,
            @PathVariable Long notificationId
    ) {
        Integer userId = authService.getUserIdFromRequest(request);

        notificationService.markNotification(userId, notificationId);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "알림 읽음 처리 완료", null
        ));
    }

    @GetMapping("/notifications/test")
    public ResponseEntity<CommonResponse<List<NotificationDTO>>> testNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {
        List<NotificationDTO> response = notificationService.getPagedNotifications(page, size);
        return ResponseEntity.ok(new CommonResponse<>(200, "테스트용 알림 조회 완료", response));
    }

    /**
     * 성능 테스트용 API
     */
    @GetMapping("/all-unpaged")
    public ResponseEntity<CommonResponse<NotificationListWithCursorDTO>> getAllNotificationsUnpaged(
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        NotificationListWithCursorDTO response = notificationService.getAllNotifications(userId);
        return ResponseEntity.ok(new CommonResponse<>(200, "전체 알림 조회 완료", response));
    }


}
