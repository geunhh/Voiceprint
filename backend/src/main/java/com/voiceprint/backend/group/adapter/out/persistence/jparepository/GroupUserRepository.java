package com.voiceprint.backend.group.adapter.out.persistence.jparepository;

import com.voiceprint.backend.group.adapter.in.web.dto.UserInfoDTO;
import com.voiceprint.backend.group.adapter.out.persistence.GroupJpaEntity;
import com.voiceprint.backend.group.adapter.out.persistence.GroupUserJpaEntity;
import com.voiceprint.backend.group.domain.GroupUserId;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUserJpaEntity, GroupUserId> {

    /**
     * 그룹 내 ADMIN 역할 사용자 찾기
     */
    GroupUserJpaEntity findByGroupIdAndRole(Integer groupId, GroupUserJpaEntity.Role role);

    Optional<GroupUserJpaEntity> findByGroupIdAndUserId(Integer groupId, Integer userId);

    List<GroupUserJpaEntity> findAllByGroupId(Integer groupId);

    @Query("SELECT new com.voiceprint.backend.group.adapter.in.web.dto.UserInfoDTO(u.id, p.imageUrl, u.nickname) " +
            "FROM GroupUser gu JOIN gu.user u " +
            "JOIN u.profileImage p " +
            "WHERE gu.group.id = :groupId")
    List<Optional<UserInfoDTO>> findUserInfoByGroupId(@Param("groupId") Integer groupId);


    @Query("""
            select  gu.user from GroupUser gu 
            where gu.group.id = :groupId
            """)
    List<UserJPAEntity> findUsersByGroupId(@Param("groupId") Integer groupId);

    boolean existsByGroupAndUser(GroupJpaEntity group, UserJPAEntity user);

    boolean existsByUserIdAndGroupId(Integer userId, Integer groupId);

    @Query("SELECT gu.group.id FROM GroupUser gu WHERE gu.user.id = :userId")
    List<Integer> findGroupIdsByUserId(@Param("userId") Integer userId);
}

