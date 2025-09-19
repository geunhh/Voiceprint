package com.voiceprint.backend.group.application.port.in.groupdiary;

import com.voiceprint.backend.group.adapter.in.web.dto.GroupDiaryDetailResponse;

public interface GetGroupDiaryDetailUseCase {
    GroupDiaryDetailResponse getGroupDiaryDetail(Integer userId, Integer groupId, Integer diaryId);
}
