package com.voiceprint.backend.service.groups;

import com.voiceprint.backend.group.adapter.in.web.dto.InviteAcceptResponseDTO;
import com.voiceprint.backend.group.adapter.in.web.dto.InviteCodeResponseDTO;
import com.voiceprint.backend.group.adapter.in.web.dto.InviteInfoReponseDTO;
import com.voiceprint.backend.global.exception.group.*;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.*;
import com.voiceprint.backend.domain.Repository.*;
import com.voiceprint.backend.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.backend.notification.domain.Notification;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupInviteService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GroupUserRepository groupUserRepository;
    private final NotificationRepositoryPort notificationPort;

    /**
     * 그룹 초대 코드를 생성하는 메소드
     * 유효한 초대 코드가 있을 경우 반환, 없으면 생성 후 반환
     */
    @Transactional(readOnly = false)
    public InviteCodeResponseDTO createInvite(Integer groupId, Integer userId) {
        // 그룹 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다."));

        // 유저 조회
        UserJPAEntity user = userRepository.findById(userId)
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


        return new InviteCodeResponseDTO(invite.getInviteCode());
    }

    /**
     * 그룹 코드에 대한 그룹 정보를 조회하는 메서드
     */
    public InviteInfoReponseDTO getInviteInfo(String code, Integer userId) {
        // 초대 정보 확인
        GroupInvite invite = groupInviteRepository.findByInviteCodeWithGroup(code)
                .orElseThrow(() -> new InviteNotFoundException("초대 코드를 찾을 수 없습니다."));

        // 유효성 검사
        if (!invite.isUsable()) {
            throw new InviteExpiredException("초대 코드가 만료되었거나 더 이상 유효하지 않습니다.");
        }
        log.debug("이미 등록된 유저인가??");
        // 이미 등록된 유저인가??
        boolean alreadyJoined = groupUserRepository
                .existsByUserIdAndGroupId(userId, invite.getGroup().getId());


        return new InviteInfoReponseDTO(
                invite.getGroup().getName(),
                invite.getGroup().getGroupImage(),
                invite.getExpiredAt(),
                alreadyJoined
        );
    }

    /**
     * 초대를 수락하는 메서드
     */
    @Transactional
    public InviteAcceptResponseDTO acceptInvite(String code, Integer userId) {

        // 초대 코드 확인
        GroupInvite invite = groupInviteRepository.findByInviteCodeWithGroup(code)
                .orElseThrow(() -> new InviteNotFoundException("초대 코드를 찾을 수 없습니다."));

        if (invite.isExpired()) {
            throw new InviteExpiredException("초대 코드가 만료되었거나 더 이상 유효하지 않습니다.");
        }

        // 그룹 조회
        Group group = invite.getGroup();
        log.info("group : {} and groupId :{}",group, group.getId());

        // 유저 조회
        UserJPAEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보가 없습니다."));

        boolean alreadyMember = groupUserRepository.existsByUserIdAndGroupId(userId, group.getId());

        if (alreadyMember) {
            log.debug("이미 참여중인 사용자입니다. alreadyMember : {}", alreadyMember);
            throw new AlreadyJoinedGroupException("이미 해당 그룹에 참여 중입니다.");
        }

        // 그룹 - 사용자 연관 관계 생성
        GroupUser groupUser = GroupUser.builder()
                .id(new GroupUserId(userId, group.getId()))
                .group(group)
                .user(user)
                .role(GroupUser.Role.MEMBER)
                .build();

        groupUserRepository.save(groupUser);
        log.info("groupUser : {} ",groupUser);

        return new InviteAcceptResponseDTO(
                group.getId()
        );
    }

    @Transactional
    public List<Notification> saveAndSendNewMember(Integer groupId, Integer userId) {
        UserJPAEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보가 없습니다."));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("그룹 정보가 없습니다."));

        List<UserJPAEntity> users = groupUserRepository.findUsersByGroupId(groupId);

        List<Notification> toSaveNotifications = new ArrayList<>();

        // 2. 알림 생성 및 전송
        for (UserJPAEntity member : users) {
            if (member.getId().equals(userId)) continue;

            //메타 데이터 구성
            Map<String, Object> metadata = Map.of(
                    "groupId", groupId
            );

            String message = user.getNickname() + "님이 " + group.getName() + " 그룹에 참여했어요.! 환영해주세요!!!";

            // 알림 Entity 직접 생성
            Notification notification = Notification.create(
                    member.getId(),
                    "newMember",
                    message,
                    metadata
            );
            toSaveNotifications.add(notification);
        }
        notificationPort.saveAll(toSaveNotifications);

        return toSaveNotifications;
    }
}