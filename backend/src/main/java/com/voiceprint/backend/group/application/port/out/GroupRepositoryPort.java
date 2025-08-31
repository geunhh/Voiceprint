package com.voiceprint.backend.group.application.port.out;

import com.voiceprint.backend.group.domain.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepositoryPort {

    Group save(Group group);

    Optional<Group> findById(Integer id);

    List<Group> findAllByUserId(Integer userId);

    void deleteById(Integer id);

}
