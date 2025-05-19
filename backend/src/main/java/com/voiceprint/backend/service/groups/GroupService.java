package com.voiceprint.backend.service.groups;


import com.voiceprint.backend.api.groups.dto.*;
import com.voiceprint.backend.common.exception.group.UnauthorizedGroupAccessException;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.domain.Entity.Group;
import com.voiceprint.backend.domain.Entity.GroupUser;
import com.voiceprint.backend.domain.Entity.GroupUserId;
import com.voiceprint.backend.domain.Repository.GroupDiaryRepository;
import com.voiceprint.backend.domain.Repository.GroupRepository;
import com.voiceprint.backend.domain.Repository.GroupUserRepository;
import com.voiceprint.backend.service.S3Service;
import com.voiceprint.backend.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupDiaryRepository groupDiaryRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final AuthService authService;

    @Transactional
    public GroupCreateResponse createGroup(Long userId, GroupCreateRequest request) {
        // 유저 조회
        User createUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));;
        // 이미지 업로드
        String imageUrl = null;
        if (request.getGroupImage() != null && !request.getGroupImage().isEmpty()) {
            imageUrl = s3Service.uploadFile(request.getGroupImage(), "group");
        }
        // 그룹 엔티티 생성
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .groupImage(imageUrl)
                .enableAlarm(request.getEnableAlarm() != null ? request.getEnableAlarm() : false)
                .alarmDays(request.getAlarmDays())
                .alarmTime(request.getAlarmTime())
                .build();

        // 그룹 저장
        Group savedGroup = groupRepository.save(group);

        // 그룹 유저 엔티티 생성
        GroupUser groupUser = GroupUser.builder()
                .id(new GroupUserId(userId, savedGroup.getId()))
                .user(createUser)  // 사용자 ID 설정
                .group(savedGroup)  // 그룹 설정
                .role(GroupUser.Role.ADMIN)   // 생성자는 ADMIN으로 설정
                .build();

        // 그룹 유저 저장
        groupUserRepository.save(groupUser);

        // 응답 객체 반환
        return new GroupCreateResponse(savedGroup.getId(), savedGroup.getName(),
                savedGroup.getDescription(), savedGroup.getGroupImage(),
                groupUser.getRole().name(),
                group.getEnableAlarm(),
                group.getAlarmTime() != null ? group.getAlarmTime().toString() : null,
                group.getAlarmDays() != null ? group.getAlarmDays().toString() : null);
    }

    public GroupUpdateResponse updateGroup(Long groupId, Long userId, GroupUpdateRequest updateRequest) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        GroupUser groupUser = groupUserRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedGroupAccessException("그룹 접근 권한이 없습니다."));

        // 권한 확인
        if (!groupUser.getRole().equals(GroupUser.Role.ADMIN)) {
            throw new UnauthorizedGroupAccessException("ADMIN만 수정 가능합니다.");
        }

        if (updateRequest.getName() != null) group.setName(updateRequest.getName());
        if (updateRequest.getDescription() != null) group.setDescription(updateRequest.getDescription());
        if (updateRequest.getIsDeleted() != null) group.setIsDeleted(updateRequest.getIsDeleted());
        if (updateRequest.getEnableAlarm() != null) group.setEnableAlarm(updateRequest.getEnableAlarm());
        if (updateRequest.getAlarmDays() != null && !updateRequest.getAlarmDays().isEmpty()) group.setAlarmDays(updateRequest.getAlarmDays());
        if (updateRequest.getAlarmTime() != null) group.setAlarmTime(updateRequest.getAlarmTime());

        if (updateRequest.getGroupImage() != null) {
            if (group.getGroupImage() != null) {
                s3Service.deleteFile(group.getGroupImage());
            }
            String newImageUrl = s3Service.uploadFile(updateRequest.getGroupImage(),"group");
            group.setGroupImage(newImageUrl);
        }

        groupRepository.save(group);

        return GroupUpdateResponse.from(group);
    }

    @Transactional(readOnly = true)
    public GroupMainPageResponse getGroupMainPage(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (group.getIsDeleted()) {
            throw new IllegalArgumentException("삭제된 그룹입니다.");
        }

        GroupUser groupUser = groupUserRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹에 참여하지 않았습니다."));

        // 그룹에 속한 유저들
        List<UserInfoDTO> groupUserList = groupUserRepository.findUserInfoByGroupId(groupId);


        return new GroupMainPageResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getEnableAlarm(),
                group.getAlarmDays(),
                group.getAlarmTime(),
                group.getCreatedAt(),
                groupUserList,
                groupUser.getJoinedAt(),
                group.getGroupImage()
        );
    }

    @Transactional(readOnly = true)
    public List<MyGroupResponse> getMyGroups(Long userId) {
        List<Group> groups = groupRepository.findAllByUserId(userId);

        return groups.stream().map(group -> {
            List<GroupUser> groupUsers = groupUserRepository.findAllByGroupId(group.getId());
            int memberCount = groupUsers.size();
            List<String> profileImages = groupUsers.stream()
                    .limit(3)
                    .map(gu -> gu.getUser().getProfileImage().getImageUrl())
                    .collect(Collectors.toList());

            return new MyGroupResponse(
                    group.getId(),
                    group.getName(),
                    group.getGroupImage(),
                    memberCount,
                    profileImages,
                    group.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }
}
