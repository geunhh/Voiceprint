package com.voiceprint.backend.service.groups;


import com.voiceprint.backend.api.groups.dto.GroupCreateRequest;
import com.voiceprint.backend.api.groups.dto.GroupCreateResponse;
import com.voiceprint.backend.domain.*;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupUserRepository groupUserRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupCreateResponse createGroup(Long userId, GroupCreateRequest request) {
        User createUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));;

        // 그룹 엔티티 생성
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .groupImage(request.getGroupImage())
                .build();

        // 그룹 저장
        Group savedGroup = groupRepository.save(group);

        // 그룹 유저 엔티티 생성
        GroupUser groupUser = GroupUser.builder()
                .id(new GroupUserId(userId, savedGroup.getId()))
                .user(createUser)  // 사용자 ID 설정
                .group(savedGroup)  // 그룹 설정
                .role(GroupUser.Role.ADMIN)   // 생성자는 ADMIN으로 설정
                .enableAlarm(request.getEnableAlarm() != null ? request.getEnableAlarm() : false)
                .alarm(request.getAlarm())
                .build();

        // 그룹 유저 저장
        groupUserRepository.save(groupUser);

        // 응답 객체 반환
        return new GroupCreateResponse(savedGroup.getId(), savedGroup.getName(),
                savedGroup.getDescription(), savedGroup.getGroupImage(),
                groupUser.getRole().name());
    }
}
