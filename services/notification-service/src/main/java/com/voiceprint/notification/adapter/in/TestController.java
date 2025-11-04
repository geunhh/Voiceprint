package com.voiceprint.notification.adapter.in;

import com.voiceprint.notification.application.service.NotificationTestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final NotificationTestService testService;

    /**
     * 예:
     * POST /api/notifications/test?time=06:30&limit=500
     * POST /api/notifications/test?time=06:30&userIds=1,2,3
     */
    @PostMapping("/alarm")
    public ResponseEntity<NotificationTestService.NotifyTestResult> trigger(
            @RequestParam(value = "time", required = false) String time,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "userIds", required = false) String userIdsCsv
    ) {
        LocalTime testTime = (time == null || time.isBlank())
                ? LocalTime.now().withSecond(0).withNano(0)                       // 기본: 현재 분
                : LocalTime.parse(time);                                          // "HH:mm" 형식 기대

        List<Integer> onlyUserIds = parseCsvToInts(userIdsCsv);

        var result = testService.trigger(testTime, limit, onlyUserIds);
        return ResponseEntity.ok(result);
    }

    private List<Integer> parseCsvToInts(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }

    /**
     * 알림 테스트용 API
     */
    @GetMapping("/")
    public ResponseEntity<String> getUnreadNotifications(
            HttpServletRequest request
    ) {
        log.info("test 입니다. {}",request);


        return ResponseEntity.ok("gggg"   );
    }


}
