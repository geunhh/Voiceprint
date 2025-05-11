package com.voiceprint.backend.service.groups;


import com.voiceprint.backend.api.groups.dto.GroupCreateRequest;
import com.voiceprint.backend.api.groups.dto.GroupCreateResponse;
import com.voiceprint.backend.api.groups.dto.GroupUpdateRequest;
import com.voiceprint.backend.api.groups.dto.GroupUpdateResponse;
import com.voiceprint.backend.common.exception.group.UnauthorizedGroupAccessException;
import com.voiceprint.backend.domain.*;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import com.voiceprint.backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

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
                group.getAlarmTime().toString(),
                group.getAlarmDays().toString());
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
}
