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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notifications")
public class SseController {
    private final SseService sseService;
    private final AuthService authService;
    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            HttpServletRequest request
    ) {
        Long userId = authService.getUserIdFromRequest(request);

        return sseService.subscribe(userId);
    }

    /**
     * 알림 테스트용 API
     */
    @GetMapping(value = "/subscribe/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testSubscribe() {
        Long testUserId = 1L; // 임시 유저
        return sseService.subscribe(testUserId);
    }

    /**
     * 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<CommonResponse<NotificationListWithCursorDTO>> getUnreadNotifications(
            HttpServletRequest request,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
//        Long userId = authService.getUserIdFromRequest(request);
        Long userId=1L;
        log.info("userid : {}, 알림 조회 ",userId);
        NotificationListWithCursorDTO response = notificationService.getUnreadNotifications(userId,cursor, size);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "알림 조회 성공", response
        ));

    }


}
