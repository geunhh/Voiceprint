package com.voiceprint.notification.adapter.in.web;

import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.notification.adapter.in.web.NotificationListWithCursorDTO;
import com.voiceprint.notification.application.port.in.NotificationUseCase;
import com.voiceprint.notification.application.service.SseService;
import com.voiceprint.backend.user.application.port.in.GetUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notifications")
public class NotificationController {
    private final SseService sseService;
    private final GetUserUseCase authService;
    private final NotificationUseCase notificationService;

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
    @GetMapping(value = {"", "/"})
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

}
