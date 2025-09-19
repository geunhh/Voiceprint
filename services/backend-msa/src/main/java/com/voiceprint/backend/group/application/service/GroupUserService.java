package com.voiceprint.backend.group.application.service;

import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.group.application.port.out.GroupUserRepositoryPort;
import com.voiceprint.backend.group.domain.GroupUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voiceprint.backend.group.application.port.in.groupuser.PromoteToAdminUseCase;


@Service
@Transactional
@RequiredArgsConstructor
public class GroupUserService implements PromoteToAdminUseCase { // Implements clause added

    private final GroupUserRepositoryPort groupUserRepositoryPort; // Changed

    /**
     * 새로운 관리자로 승급할 때 기존 관리자 강등
     */
    @Override // Add @Override annotation
    public ResponseEntity<CommonResponse<String>> promoteToAdmin(Integer groupId, Integer currentAdminId, Integer newAdminUserId) {
        // 현재 ADMIN 확인
        GroupUser currentAdmin = groupUserRepositoryPort.findByGroupIdAndUserId(groupId, currentAdminId)
                .orElseThrow(() -> new IllegalArgumentException("현재 관리자를 찾을 수 없습니다."));

        // 요청한 userId와 현재 ADMIN의 userId가 일치하지 않으면 권한 없음
        if (!currentAdmin.getUser().getId().equals(currentAdminId)) { // Use pure User object
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 새로운 관리자로 지정할 사용자 찾기
        GroupUser newAdmin = groupUserRepositoryPort.findByGroupIdAndUserId(newAdminUserId, groupId) // Use port
                .orElseThrow(() -> new IllegalArgumentException("새로운 관리자를 찾을 수 없습니다."));

        // 기존 ADMIN을 MEMBER로 강등
        GroupUser demotedAdmin = currentAdmin.toBuilder().role(GroupUser.Role.MEMBER).build();
        groupUserRepositoryPort.save(demotedAdmin);

        // 새로운 사용자를 ADMIN으로 승급
        GroupUser promotedAdmin = newAdmin.toBuilder().role(GroupUser.Role.ADMIN).build();
        groupUserRepositoryPort.save(promotedAdmin);

        return ResponseEntity.ok(new CommonResponse<>(200, currentAdminId + " (이)가 " + newAdminUserId + "에게 관리자 위임 완료", null));
    }
}