package com.voiceprint.notification.adapter.in;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class TestController {

    /**
     * 알림 테스트용 API
     */
    @GetMapping("/")
    public ResponseEntity<String> getUnreadNotifications(
            HttpServletRequest request
    ) {

        return ResponseEntity.ok("gggg"   );
    }


}
