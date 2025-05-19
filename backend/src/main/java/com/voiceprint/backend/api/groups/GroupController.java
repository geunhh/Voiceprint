package com.voiceprint.backend.api.groups;

import com.voiceprint.backend.api.groups.dto.*;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.diary.GroupDiaryService;
import com.voiceprint.backend.service.groups.GroupService;
import com.voiceprint.backend.service.groups.GroupUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupUserService groupUserService;
    private final GroupService groupService;
    private final AuthService authService;
    private final GroupDiaryService groupDiaryService;

    /**
     * 그룹 생성
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse<GroupCreateResponse>> createGroup(HttpServletRequest request,
                                                                           @ModelAttribute GroupCreateRequest requestData) {

        Integer userId = authService.getUserIdFromRequest(request);
        GroupCreateResponse response = groupService.createGroup(userId, requestData);
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
        GroupMainPageResponse response = groupService.getGroupMainPage(groupId, userId);

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

        GroupUpdateResponse updatedGroup = groupService.updateGroup(groupId, userId, updateRequest);

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

        GroupDiaryListWithCursorDTO result = groupDiaryService.getGroupDiaries(request, groupId, cursor, size);
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
        GroupDiaryListWithCursorDTO result = groupDiaryService.getAllGroupDiaries(request, cursor, size);
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
        GroupDiaryDetailResponse response = groupDiaryService.getGroupDiaryDetail(userId, groupId, diaryId);
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
        return groupUserService.promoteToAdmin(groupId, userId, newUserId);
    }
    /**
     * 내가 속한 그룹 조회
     */
    @GetMapping("/my")
    public ResponseEntity<CommonResponse<List<MyGroupResponse>>> getMyGroups(HttpServletRequest request) {
        Integer userId = authService.getUserIdFromRequest(request);
        List<MyGroupResponse> myGroups = groupService.getMyGroups(userId);
        return ResponseEntity.ok(new CommonResponse<>(200, "내 그룹 목록 조회 성공", myGroups));
    }
}

