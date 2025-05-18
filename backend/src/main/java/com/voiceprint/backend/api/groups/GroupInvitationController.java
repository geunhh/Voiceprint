package com.voiceprint.backend.api.groups;

import com.voiceprint.backend.api.groups.dto.GroupCreateRequest;
import com.voiceprint.backend.api.groups.dto.GroupCreateResponse;
import com.voiceprint.backend.api.groups.dto.InviteCreateResponseDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.diary.GroupDiaryService;
import com.voiceprint.backend.service.groups.GroupInviteService;
import com.voiceprint.backend.service.groups.GroupService;
import com.voiceprint.backend.service.groups.GroupUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/group")
@Slf4j
@RequiredArgsConstructor
public class GroupInvitationController {

    private final AuthService authService;
    private final GroupInviteService groupInviteService;

    /**
     * 그룹 초대 코드 조회 및 생성 API
     * 유효 기간 1시간.
     */
    @PostMapping("/{groupId}/invites")
    public ResponseEntity<CommonResponse<InviteCreateResponseDTO>> createInvite(
            HttpServletRequest request,
            @PathVariable Long groupId)
    {
//        Long userId = authService.getUserIdFromRequest(request);
        Long userId = 1L;
        log.info("그룹 초대 코드 생성 및 조회 API 호출");
        InviteCreateResponseDTO response = groupInviteService.createInvite(groupId, userId);
        log.debug("초대 코드 : {} ",response.getInviteCode());

        return ResponseEntity.ok(new CommonResponse<>(200, "초대 링크 조회 완료", response));
    }

}
