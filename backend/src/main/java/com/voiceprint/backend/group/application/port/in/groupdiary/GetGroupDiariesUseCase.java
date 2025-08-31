package com.voiceprint.backend.group.application.port.in.groupdiary;

import com.voiceprint.backend.group.adapter.in.web.dto.GroupDiaryListWithCursorDTO;
import java.time.LocalDateTime;

public interface GetGroupDiariesUseCase {
    GroupDiaryListWithCursorDTO getGroupDiaries(Integer groupId, LocalDateTime cursor, Integer size, Integer userId);
}
