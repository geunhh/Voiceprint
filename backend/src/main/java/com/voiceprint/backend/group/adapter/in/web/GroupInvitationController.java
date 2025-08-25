package com.voiceprint.backend.group.adapter.in.web;

import com.voiceprint.backend.group.adapter.in.web.dto.InviteAcceptResponseDTO;
import com.voiceprint.backend.group.adapter.in.web.dto.InviteCodeRequestDTO;
import com.voiceprint.backend.group.adapter.in.web.dto.InviteCodeResponseDTO;
import com.voiceprint.backend.group.adapter.in.web.dto.InviteInfoReponseDTO;
import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.domain.Entity.Notification;
import com.voiceprint.backend.service.alarm.NotificationService;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.groups.GroupInviteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/group")
@Slf4j
@RequiredArgsConstructor
public class GroupInvitationController {

    private final AuthService authService;
    private final GroupInviteService groupInviteService;
    private final NotificationService notificationService;

    /**
     * 그룹 초대 코드 조회 및 생성 API
     * 유효 기간 1시간.
     */
    @PostMapping("/{groupId}/invites")
    public ResponseEntity<CommonResponse<InviteCodeResponseDTO>> createInvite(
            HttpServletRequest request,
            @PathVariable Integer groupId)
    {
        Integer userId = authService.getUserIdFromRequest(request);

        log.info("그룹 초대 코드 생성 및 조회 API 호출");
        InviteCodeResponseDTO response = groupInviteService.createInvite(groupId, userId);
        log.debug("초대 코드 : {} ",response.getInviteCode());

        return ResponseEntity.ok(new CommonResponse<>(200, "초대 링크 조회 완료", response));
    }

    @GetMapping("/invite-info")
    public ResponseEntity<CommonResponse<InviteInfoReponseDTO>> getInviteInfo(
            @RequestParam("code") String code,
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);

        InviteInfoReponseDTO response = groupInviteService.getInviteInfo(code, userId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "초대 정보 조회 성공", response
        ));
    }

    @PostMapping("/invite/accept")
    public ResponseEntity<CommonResponse<InviteAcceptResponseDTO>> acceptInvite(
            HttpServletRequest httprequest,
            @RequestBody InviteCodeRequestDTO request
    ) {
        Integer userId = authService.getUserIdFromRequest(httprequest);
        log.info("{} 유저가 그룹 초대를 수락하려고 합니다.",userId);

        InviteAcceptResponseDTO response = groupInviteService.acceptInvite(request.getInviteCode(), userId);
        CompletableFuture.runAsync(() -> {
            List<Notification> notifications = groupInviteService.saveAndSendNewMember(response.getGroupId(), userId);
            notificationService.publishAllNotifications(notifications);
        });
        return ResponseEntity.ok(new CommonResponse<>(
                200, "초대 요청 처리 완료", response
        ));
    }

}
