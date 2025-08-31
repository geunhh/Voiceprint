package com.voiceprint.backend.group.application.service;

import com.voiceprint.backend.group.adapter.in.web.dto.*;
import com.voiceprint.backend.global.exception.group.GroupNotFoundException;
import com.voiceprint.backend.global.exception.group.UnauthorizedGroupAccessException;
import com.voiceprint.backend.group.application.port.out.GroupRepositoryPort;
import com.voiceprint.backend.group.application.port.out.GroupUserRepositoryPort;
import com.voiceprint.backend.group.domain.Group;
import com.voiceprint.backend.group.domain.GroupUser;
import com.voiceprint.backend.group.domain.GroupUserId;
import com.voiceprint.backend.global.util.S3Service;
import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;
import com.voiceprint.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.voiceprint.backend.group.application.port.in.group.CreateGroupUseCase;
import com.voiceprint.backend.group.application.port.in.group.UpdateGroupUseCase;
import com.voiceprint.backend.group.application.port.in.group.GetGroupMainPageUseCase;
import com.voiceprint.backend.group.application.port.in.group.GetMyGroupsUseCase;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class GroupService implements CreateGroupUseCase, UpdateGroupUseCase, GetGroupMainPageUseCase, GetMyGroupsUseCase { // Implements clause added

    private final GroupRepositoryPort groupRepositoryPort;
    private final GroupUserRepositoryPort groupUserRepositoryPort;
    private final UserRepositoryPort userRepositoryPort; // User 도메인 리팩토링 시 UserPort로 변경 필요
    private final S3Service s3Service;

    // S3 활성화 여부 임시 플래그 (추후 설정 파일 등으로 관리)
    private final boolean isS3Active = false; // S3 서버가 내려가 있으므로 false로 설정

    @Override
    @Transactional
    public GroupCreateResponse createGroup(Integer userId, GroupCreateRequest request) {
        // 유저 조회
        User createUser = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
        // 이미지 업로드
        if (request.getGroupImage() == null || request.getGroupImage().isEmpty()) {
            throw new GroupNotFoundException("그룹 이미지가 없습니다.");
        }
        //Todo : S3 서버 임시 제거
//        String imageUrl = s3Service.uploadFile(request.getGroupImage(), "group");
        String imageUrl = "https://tempImage.com/gggg";

        // 그룹 도메인 객체 생성
        Group group = Group.create(
            request.getName(),
            request.getDescription(),
            "null", //TODO : 일단 null.
            request.getAlarmDays(),
            request.getAlarmTime(),
            request.getEnableAlarm() != null ? request.getEnableAlarm() : false
        );

        // 그룹 저장
        Group savedGroup = groupRepositoryPort.save(group);

        // 그룹 유저 엔티티 생성
        GroupUser groupUser = GroupUser.builder()
                .id(new GroupUserId(userId, savedGroup.getId()))
                .user(createUser)  // 사용자 ID 설정
                .group(savedGroup) // GroupUser 리팩토링 시 순수 Group 객체 사용
                .role(GroupUser.Role.ADMIN)   // 생성자는 ADMIN으로 설정
                .build();

        // 그룹 유저 저장
        groupUserRepositoryPort.save(groupUser);

        // 응답 객체 반환
        return new GroupCreateResponse(savedGroup.getId(), savedGroup.getName(),
                savedGroup.getDescription(), savedGroup.getGroupImage(),
                groupUser.getRole().name(),
                savedGroup.getEnableAlarm(),
                savedGroup.getAlarmTime() != null ? savedGroup.getAlarmTime().toString() : null,
                savedGroup.getAlarmDays() != null ? savedGroup.getAlarmDays().toString() : null);
    }

    @Override
    @Transactional
    public GroupUpdateResponse updateGroup(Integer groupId, Integer userId, GroupUpdateRequest updateRequest) {
        Group group = groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다."));

        GroupUser groupUser = groupUserRepositoryPort.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedGroupAccessException("그룹 접근 권한이 없습니다."));

        // 권한 확인
        if (!groupUser.getRole().equals(GroupUser.Role.ADMIN)) {
            throw new UnauthorizedGroupAccessException("ADMIN만 수정 가능합니다.");
        }

        // toBuilder를 사용하여 불변 객체의 새 버전을 생성
        Group updatedGroup = group.toBuilder()
                .name(updateRequest.getName() != null ? updateRequest.getName() : group.getName())
                .description(updateRequest.getDescription() != null ? updateRequest.getDescription() : group.getDescription())
                .enableAlarm(updateRequest.getEnableAlarm() != null ? updateRequest.getEnableAlarm() : group.getEnableAlarm())
                .alarmDays(updateRequest.getAlarmDays() != null && !updateRequest.getAlarmDays().isEmpty() ? updateRequest.getAlarmDays() : group.getAlarmDays())
                .alarmTime(updateRequest.getAlarmTime() != null ? updateRequest.getAlarmTime() : group.getAlarmTime())
                .build();

        // S3 이미지 처리 로직
        String finalGroupImage = updatedGroup.getGroupImage(); // 현재 이미지로 초기화
        if (updateRequest.getGroupImage() != null) {
            if (isS3Active) { // S3가 활성화된 경우에만 실제 S3 호출
                // 기존 이미지 삭제 로직
                if (group.getGroupImage() != null && !group.getGroupImage().isEmpty() && !group.getGroupImage().equals("null")) {
                    s3Service.deleteFile(group.getGroupImage());
                }
                // 새 이미지 업로드
                finalGroupImage = s3Service.uploadFile(updateRequest.getGroupImage(), "group");
            } else {
                // S3 비활성화 시 임시 URL 반환
                finalGroupImage = "https://temp.image.url/updated_group_image"; // 임시 URL
            }
        }
        updatedGroup = updatedGroup.toBuilder().groupImage(finalGroupImage).build(); // 이미지 URL 업데이트

        Group resultGroup = groupRepositoryPort.save(updatedGroup);

        return GroupUpdateResponse.from(resultGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupMainPageResponse getGroupMainPage(Integer groupId, Integer userId) {
        Group group = groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다."));

        GroupUser groupUser = groupUserRepositoryPort.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedGroupAccessException("해당 그룹에 참여하지 않았습니다."));

        // 그룹에 속한 유저들
        List<UserInfoDTO> groupUserList = groupUserRepositoryPort
                .findAllByGroupId(groupId).stream()
                .map(gu -> UserInfoDTO.from(gu.getUser(), "https://temp.image.url/" + gu.getUser().getProfileImageId())) // UserInfoDTO.from 사용
                .collect(Collectors.toList());

        return GroupMainPageResponse.from(
                group,
                groupUser,
                groupUserList
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyGroupResponse> getMyGroups(Integer userId) {
        List<Group> groups = groupRepositoryPort.findAllByUserId(userId);

        return groups.stream().map(group -> {
            List<GroupUser> groupUsers = groupUserRepositoryPort.findAllByGroupId(group.getId());
            int memberCount = groupUsers.size();
            List<String> profileImages = groupUsers.stream()
                    .limit(3)
                    .map(gu -> "https://temp.image.url/" + gu.getUser().getProfileImageId()) // 임시 URL 사용
                    .collect(Collectors.toList());

            return MyGroupResponse.from(
                    group,
                    memberCount,
                    profileImages
            );
        }).collect(Collectors.toList());
    }
}
