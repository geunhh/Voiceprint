package com.voiceprint.backend.api.alarm;

import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.alarm.NotificationService;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    // 알림 목록 조회

    /**
     * 알림 목록 조회
     */
//    @GetMapping
//    public ResponseEntity<CommonResponse<Page<NotificationDTO>>> getNotifications(
//            HttpServletRequest request,
//            Pageable pageable
//    ) {
//        Long userId= authService.getUserIdFromRequest(request);
//        Page<NotificationDTO> response = notificationService.getUserNotification(userId,pageable);
//
//        return ResponseEntity.ok(new CommonResponse<>(
//                200, "성고옹", response));
//
//    }
}