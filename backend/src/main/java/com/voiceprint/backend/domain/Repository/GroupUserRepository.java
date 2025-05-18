package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.api.groups.dto.UserInfoDTO;
import com.voiceprint.backend.domain.Entity.Group;
import com.voiceprint.backend.domain.Entity.GroupUser;
import com.voiceprint.backend.domain.Entity.GroupUserId;
import com.voiceprint.backend.domain.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, GroupUserId> {

    /**
     * 그룹 내 ADMIN 역할 사용자 찾기
     */
    GroupUser findByGroupIdAndRole(Long groupId, GroupUser.Role role);

    Optional<GroupUser> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupUser> findAllByGroupId(Long groupId);

    @Query("SELECT new com.voiceprint.backend.api.groups.dto.UserInfoDTO(u.id, p.imageUrl, u.nickname) " +
            "FROM GroupUser gu JOIN gu.user u " +
            "JOIN u.profileImage p " +
            "WHERE gu.group.id = :groupId")
    List<UserInfoDTO> findUserInfoByGroupId(@Param("groupId") Long groupId);


    @Query("""
            select  gu.user from GroupUser gu 
            where gu.group.id = :groupId
            """)
    List<User> findUsersByGroupId(@Param("groupId") Long groupId);

    boolean existsByGroupAndUser(Group group, User user);
}

