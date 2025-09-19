package com.voiceprint.backend.group.application.port.in.group;

import com.voiceprint.backend.group.adapter.in.web.dto.GroupUpdateResponse;
import com.voiceprint.backend.group.adapter.in.web.dto.GroupUpdateRequest;

public interface UpdateGroupUseCase {
    GroupUpdateResponse updateGroup(Integer groupId, Integer userId, GroupUpdateRequest updateRequest);
}
