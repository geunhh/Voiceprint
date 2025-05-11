package com.voiceprint.backend.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUserId> {

    /**
     * 그룹 내 ADMIN 역할 사용자 찾기
     */
    GroupUser findByGroupIdAndRole(Long groupId, GroupUser.Role role);

    Optional<GroupUser> findByGroupIdAndUserId(Long groupId, Long userId);
}

