package com.voiceprint.backend.group.application.port.in.groupdiary;

import com.voiceprint.backend.group.adapter.in.web.dto.GroupDiaryListWithCursorDTO;
import java.time.LocalDateTime;

public interface GetAllGroupDiariesUseCase {
    GroupDiaryListWithCursorDTO getAllGroupDiaries(LocalDateTime cursor, int size, Integer userId);
}
