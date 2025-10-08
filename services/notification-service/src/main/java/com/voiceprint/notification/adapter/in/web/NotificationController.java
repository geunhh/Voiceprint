package com.voiceprint.notification.adapter.in.web;

import com.voiceprint.common.auth.JWTUtil;
import com.voiceprint.notification.application.port.in.NotificationCommandPort;
import com.voiceprint.notification.application.port.in.NotificationQueryPort;
import com.voiceprint.notification.application.service.SseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.voiceprint.notification.global.dto.CommonResponse;
import com.voiceprint.notification.adapter.in.web.dto.NotificationListWithCursorDTO;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notifications")
public class NotificationController {
    private final SseService sseService;
    private final NotificationCommandPort notificationCommandPort;
    private final NotificationQueryPort notificationQueryPort;
    private final JWTUtil jwtUtil;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            HttpServletRequest request
    ) {
        Integer userId = getUserIdFromRequest(request);
        return sseService.subscribe(userId);
    }

    //   알림 테스트용 API
    @GetMapping(value = "/subscribe/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testSubscribe() {
        Integer testUserId = 1; // 임시 유저
        return sseService.subscribe(testUserId);
    }

     //알림 목록 조회 API
    @GetMapping(value = {"", "/"})
    public ResponseEntity<CommonResponse<NotificationListWithCursorDTO>> getUnreadNotifications(
            HttpServletRequest request,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Integer userId = getUserIdFromRequest(request);
        log.info("userid : {}, 알림 조회 ",userId);
        NotificationListWithCursorDTO response = notificationQueryPort.getUnreadNotifications(userId,cursor, size);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "알림 조회 성공", response
        ));
    }

    //알림을 읽음 처리하는 API
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<CommonResponse<Void>> readNotification(
            HttpServletRequest request,
            @PathVariable Long notificationId
    ) {
        Integer userId = getUserIdFromRequest(request);

        notificationCommandPort.markNotification(userId, notificationId);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "알림 읽음 처리 완료", null
        ));
    }

    // Helper method to extract userId from request
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token = jwtUtil.extractTokenFromHeader(authorizationHeader);

        if (token == null || !jwtUtil.validateToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다."); // Using RuntimeException temporarily
        }

        Integer userId = jwtUtil.getUserId(token);
        if (userId == null) {
            throw new RuntimeException("토큰에서 사용자 ID를 추출할 수 없습니다."); // Using RuntimeException temporarily
        }
        return userId;
    }
}
