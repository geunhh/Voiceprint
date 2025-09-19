package com.voiceprint.backend.group.application.port.in.groupdiary;

import com.voiceprint.backend.notification.domain.Notification;
import java.util.List;

public interface SaveSharedDiaryUseCase {
    List<Notification> saveSharedDiary(Integer diaryId, Integer userId, List<Integer> groupIds);
}
