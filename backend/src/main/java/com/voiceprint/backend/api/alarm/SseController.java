package com.voiceprint.backend.api.alarm;

import com.voiceprint.backend.api.alarm.dto.NotificationDTO;
import com.voiceprint.backend.service.alarm.SseService;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {
    private final SseService sseService;
    private final AuthService authService;

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


}
