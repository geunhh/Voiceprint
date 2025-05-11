package com.voiceprint.backend.api.groups;

import com.voiceprint.backend.api.groups.dto.GroupCreateRequest;
import com.voiceprint.backend.api.groups.dto.GroupCreateResponse;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.groups.GroupService;
import com.voiceprint.backend.service.groups.GroupUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupUserController {

    private final GroupUserService groupUserService;
    private final GroupService groupService;
    private final AuthService authService;

    @PostMapping("")
    public ResponseEntity<CommonResponse<GroupCreateResponse>> createGroup(HttpServletRequest request,
                                                           @RequestBody GroupCreateRequest requestData) {

        Long userId = authService.getUserIdFromRequest(request);
        GroupCreateResponse response = groupService.createGroup(userId, requestData);
        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 생성 완료", response));
    }

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

