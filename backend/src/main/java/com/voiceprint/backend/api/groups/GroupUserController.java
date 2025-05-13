package com.voiceprint.backend.api.groups;

import com.voiceprint.backend.api.groups.dto.*;
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

    /**
     * 그룹 생성
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse<GroupCreateResponse>> createGroup(HttpServletRequest request,
                                                                           @ModelAttribute GroupCreateRequest requestData) {

        Long userId = authService.getUserIdFromRequest(request);
        GroupCreateResponse response = groupService.createGroup(userId, requestData);
        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 생성 완료", response));
    }

    /**
     * 그룹 정보 조회
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<CommonResponse<GroupMainPageResponse>> getGroupMainPage(
            @PathVariable Long groupId,
            HttpServletRequest request) {

        Long userId = authService.getUserIdFromRequest(request);
        GroupMainPageResponse response = groupService.getGroupMainPage(groupId, userId);

        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 메인 페이지 조회 완료", response));
    }

    /**
     * 그룹 정보 수정
     */
    @PatchMapping(path = "/{groupId}", consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse<GroupUpdateResponse>> updateGroup(
            @PathVariable Long groupId,
            @ModelAttribute GroupUpdateRequest updateRequest,
            HttpServletRequest request) {

        Long userId = authService.getUserIdFromRequest(request);

        GroupUpdateResponse updatedGroup = groupService.updateGroup(groupId, userId, updateRequest);

        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 수정 완료", updatedGroup));
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

