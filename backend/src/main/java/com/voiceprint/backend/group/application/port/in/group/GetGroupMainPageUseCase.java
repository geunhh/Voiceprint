package com.voiceprint.backend.group.application.port.in.group;

import com.voiceprint.backend.group.adapter.in.web.dto.GroupMainPageResponse;

public interface GetGroupMainPageUseCase {
    GroupMainPageResponse getGroupMainPage(Integer groupId, Integer userId);
}
