package com.voiceprint.backend.group.application.port.in.group;

import com.voiceprint.backend.group.adapter.in.web.dto.GroupCreateRequest;
import com.voiceprint.backend.group.adapter.in.web.dto.GroupCreateResponse;

public interface CreateGroupUseCase {
    GroupCreateResponse createGroup(Integer userId, GroupCreateRequest request);
}
