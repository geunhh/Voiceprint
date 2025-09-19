package com.voiceprint.backend.group.application.port.out;

import com.voiceprint.backend.group.domain.GroupUser;
import com.voiceprint.backend.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface GroupUserRepositoryPort {

    GroupUser save(GroupUser groupUser);

    Optional<GroupUser> findByGroupIdAndUserId(Integer groupId, Integer userId);

    List<GroupUser> findAllByGroupId(Integer groupId);

    boolean existsByUserIdAndGroupId(Integer userId, Integer groupId);

    List<Integer> findGroupIdsByUserId(Integer userId);

    List<User> findUsersByGroupId(Integer groupId);
}
