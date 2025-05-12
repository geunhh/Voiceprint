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
     * 연결 테스트용 API
     */
    @CrossOrigin(origins = "http://localhost:5173") // 또는 "*"
    @GetMapping(value = "/subscribe/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeT(HttpServletRequest request) {
        Long testuserId = 999L;
        // ✅ 연결되면 즉시 알림 한 번 보내기
        SseEmitter emitter = sseService.subscribe(testuserId);
        NotificationDTO testPayload = new NotificationDTO(
                "reminder",
                "✅ 연결 성공! 테스트 알림입니다.",
                "diary",
                null
        );
        sseService.sendNotification(testuserId, "reminder", testPayload);

        return emitter;


    }
}
