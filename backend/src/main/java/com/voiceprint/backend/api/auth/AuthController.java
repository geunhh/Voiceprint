package com.voiceprint.backend.api.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/api/v1/user/google")
    public String redirectToGoogleLogin() {
        return "redirect:/oauth2/authorization/google";  // Spring Security OAuth2 URL로 리다이렉트
    }
}