package com.voiceprint.backend.user.adapter.in.web;

import com.voiceprint.backend.user.adapter.in.web.dto.TestRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthTestController {
    @GetMapping("/login-success")
    @ResponseBody
    public String TestLogin(@RequestParam TestRequest access) {
        return "access=" + access.getAccess();  // Spring Security OAuth2 URL로 리다이렉트
    }
}
