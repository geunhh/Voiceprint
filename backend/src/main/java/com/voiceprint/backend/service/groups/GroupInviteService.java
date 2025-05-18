package com.voiceprint.backend.service.groups;

import com.voiceprint.backend.api.groups.dto.InviteCreateResponseDTO;
import com.voiceprint.backend.common.exception.group.GroupNotFoundException;
import com.voiceprint.backend.common.exception.group.UnauthorizedGroupAccessException;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.Group;
import com.voiceprint.backend.domain.Entity.GroupInvite;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.GroupInviteRepository;
import com.voiceprint.backend.domain.Repository.GroupRepository;
import com.voiceprint.backend.domain.Repository.GroupUserRepository;
import com.voiceprint.backend.domain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupInviteService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupUserRepository groupUserRepository;

    /**
     * 그룹 초대 코드를 생성하는 메소드
     * 유효한 초대 코드가 있을 경우 반환, 없으면 생성 후 반환
     */
    @Transactional(readOnly = false)
    public InviteCreateResponseDTO createInvite(Long groupId, Long userId) {
        // 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다."));

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        log.debug("groupId : {}, userId : {}",groupId,userId);

        // 그릅 소속 확인
        groupUserRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new UnauthorizedGroupAccessException("그룹에 속한 사용자만 초대 링크를 생성할 수 있습니다."));

        log.debug("초대 코드를 조회합니다.");
        // 유효한 초대코드가 이미 존재하면 재사용
        GroupInvite invite = groupInviteRepository
                .findTopByGroupIdOrderByCreatedAtDesc(groupId)
                .filter(GroupInvite::isUsable)      // 만료 확인.
                .orElseGet(() -> {
                    log.debug("유효한 초대 코드가 존재하지 않습니다. 초대 코드를 새로 생성합니다.");
                    GroupInvite newInvite = GroupInvite.create(group, user);
                    groupInviteRepository.save(newInvite);
                    return newInvite;
                });


        return new InviteCreateResponseDTO(invite.getInviteCode());
    }

}
