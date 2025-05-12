package com.voiceprint.backend.service.groups;

import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.domain.GroupUser;
import com.voiceprint.backend.domain.GroupUserId;
import com.voiceprint.backend.domain.GroupUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupUserService {

    private final GroupUserRepository groupUserRepository;

    /**
     * 새로운 관리자로 승급할 때 기존 관리자 강등
     */
    @Transactional
    public ResponseEntity<CommonResponse<String>> promoteToAdmin(Long groupId, Long currentAdminId, Long newAdminUserId) {
        // 현재 ADMIN 확인
        GroupUser currentAdmin = groupUserRepository.findByGroupIdAndRole(groupId, GroupUser.Role.ADMIN);

        // 요청한 userId와 현재 ADMIN의 userId가 일치하지 않으면 권한 없음
        if (currentAdmin == null || !currentAdmin.getUser().getId().equals(currentAdminId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 새로운 관리자로 지정할 사용자 찾기
        GroupUser newAdmin = groupUserRepository.findById(new GroupUserId(newAdminUserId, groupId))
                .orElseThrow(() -> new IllegalArgumentException("새로운 관리자를 찾을 수 없습니다."));

        // 기존 ADMIN을 MEMBER로 강등
        currentAdmin.setRole(GroupUser.Role.MEMBER);
        groupUserRepository.save(currentAdmin);

        // 새로운 사용자를 ADMIN으로 승급
        newAdmin.setRole(GroupUser.Role.ADMIN);
        groupUserRepository.save(newAdmin);
        return ResponseEntity.ok(new CommonResponse<>(200, "관리자 위임 완료",null));
    }


}
