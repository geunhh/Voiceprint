package com.voiceprint.backend.api.groups;

import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.groups.GroupUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupUserController {

    private final GroupUserService groupUserService;
    private final AuthService authService;
    /**
     * 관리자로 승급
     */
    @PostMapping("/{groupId}/promote/{newUserId}")
    public ResponseEntity<CommonResponse<String>> promoteToAdmin(@PathVariable Long groupId,
                                                         @PathVariable Long newUserId,
                                                         HttpServletRequest request) {

        Long userId = authService.getUserIdFromRequest(request);
        return groupUserService.promoteToAdmin(groupId, userId, newUserId);
    }
}

