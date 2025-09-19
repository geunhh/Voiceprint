package com.voiceprint.backend.group.application.port.in.group;

import com.voiceprint.backend.group.adapter.in.web.dto.MyGroupResponse;

import java.util.List;

public interface GetMyGroupsUseCase {
    List<MyGroupResponse> getMyGroups(Integer userId);
}
