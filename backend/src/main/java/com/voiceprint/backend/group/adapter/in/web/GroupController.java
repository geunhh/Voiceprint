package com.voiceprint.backend.group.adapter.in.web;

import com.voiceprint.backend.group.adapter.in.web.dto.*;
import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.user.application.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// Import the new Use Case interfaces
import com.voiceprint.backend.group.application.port.in.group.GetGroupMainPageUseCase;
import com.voiceprint.backend.group.application.port.in.group.GetMyGroupsUseCase;
import com.voiceprint.backend.group.application.port.in.group.CreateGroupUseCase;
import com.voiceprint.backend.group.application.port.in.group.UpdateGroupUseCase;
import com.voiceprint.backend.group.application.port.in.groupuser.PromoteToAdminUseCase;
import com.voiceprint.backend.group.application.port.in.groupdiary.GetGroupDiariesUseCase; // NEW
import com.voiceprint.backend.group.application.port.in.groupdiary.GetAllGroupDiariesUseCase; // NEW
import com.voiceprint.backend.group.application.port.in.groupdiary.GetGroupDiaryDetailUseCase; // NEW


@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final UserService authService;

    // Inject Use Case interfaces
    private final CreateGroupUseCase createGroupUseCase;
    private final UpdateGroupUseCase updateGroupUseCase;
    private final GetGroupMainPageUseCase getGroupMainPageUseCase;
    private final GetMyGroupsUseCase getMyGroupsUseCase;
    private final PromoteToAdminUseCase promoteToAdminUseCase;
    private final GetGroupDiariesUseCase getGroupDiariesUseCase; // NEW
    private final GetAllGroupDiariesUseCase getAllGroupDiariesUseCase; // NEW
    private final GetGroupDiaryDetailUseCase getGroupDiaryDetailUseCase; // NEW


    /**
     * 그룹 생성
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse<GroupCreateResponse>> createGroup(HttpServletRequest request,
                                                                           @ModelAttribute GroupCreateRequest requestData) {

        Integer userId = authService.getUserIdFromRequest(request);
        GroupCreateResponse response = createGroupUseCase.createGroup(userId, requestData);
        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 생성 완료", response));
    }

    /**
     * 그룹 정보 조회
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<CommonResponse<GroupMainPageResponse>> getGroupMainPage(
            @PathVariable Integer groupId,
            HttpServletRequest request) {

        Integer userId = authService.getUserIdFromRequest(request);
        GroupMainPageResponse response = getGroupMainPageUseCase.getGroupMainPage(groupId, userId);

        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 메인 페이지 조회 완료", response));
    }

    /**
     * 그룹 정보 수정
     */
    @PatchMapping(path = "/{groupId}", consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse<GroupUpdateResponse>> updateGroup(
            @PathVariable Integer groupId,
            @ModelAttribute GroupUpdateRequest updateRequest,
            HttpServletRequest request) {

        Integer userId = authService.getUserIdFromRequest(request);

        GroupUpdateResponse updatedGroup = updateGroupUseCase.updateGroup(groupId, userId, updateRequest);

        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 수정 완료", updatedGroup));
    }

    /**
     * 특정 그룹에 공유일기 목록 조회하는 API
     * @param size 페이지네이션 안에 들어갈 객체 갯수
     * @param cursor 다음 페이지 확인용, 마지막 페이지이면 null
     */
    @GetMapping("/{groupId}/diaries")
    public ResponseEntity<CommonResponse<GroupDiaryListWithCursorDTO>> getGroupDiaries(
            @PathVariable Integer groupId,
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(defaultValue = "7") Integer size,
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        GroupDiaryListWithCursorDTO result = getGroupDiariesUseCase.getGroupDiaries(groupId, cursor, size, userId);
        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 내 공유 일기 조회 성공", result));
    }
    /**
     * 속한 그룹 전체 공유일기 목록 조회하는 API
     * @param size 페이지네이션 안에 들어갈 객체 갯수
     * @param cursor 다음 페이지 확인용, 마지막 페이지이면 null
     */
    @GetMapping("/diaries")
    public ResponseEntity<CommonResponse<GroupDiaryListWithCursorDTO>> getAllGroupDiaries(
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(defaultValue = "7") int size,
            HttpServletRequest request
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        GroupDiaryListWithCursorDTO result = getAllGroupDiariesUseCase.getAllGroupDiaries(cursor, size, userId);
        return ResponseEntity.ok(new CommonResponse<>(200, "모든 그룹의 공유일기 조회 성공", result));
    }


    /**
     * 그룹 공유일기 상세조회 API
     */
    @GetMapping("/{groupId}/{diaryId}")
    public ResponseEntity<CommonResponse<GroupDiaryDetailResponse>> getGroupDiaryDetail(
            HttpServletRequest request,
            @PathVariable Integer groupId,
            @PathVariable Integer diaryId
    ) {
        Integer userId = authService.getUserIdFromRequest(request);
        GroupDiaryDetailResponse response = getGroupDiaryDetailUseCase.getGroupDiaryDetail(userId, groupId, diaryId);
        return ResponseEntity.ok(new CommonResponse<>(200, "그룹 일기 상세조회 성공", response));
    }

    /**
     * 관리자로 승급
     */
    @PostMapping("/{groupId}/promote/{newUserId}")
    public ResponseEntity<CommonResponse<String>> promoteToAdmin(@PathVariable Integer groupId,
                                                         @PathVariable Integer newUserId,
                                                         HttpServletRequest request) {

        Integer userId = authService.getUserIdFromRequest(request);
        return promoteToAdminUseCase.promoteToAdmin(groupId, userId, newUserId);
    }
    /**
     * 내가 속한 그룹 조회
     */
    @GetMapping("/my")
    public ResponseEntity<CommonResponse<List<MyGroupResponse>>> getMyGroups(HttpServletRequest request) {
        Integer userId = authService.getUserIdFromRequest(request);
        List<MyGroupResponse> myGroups = getMyGroupsUseCase.getMyGroups(userId);
        return ResponseEntity.ok(new CommonResponse<>(200, "내 그룹 목록 조회 성공", myGroups));
    }
}

