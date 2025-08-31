package com.voiceprint.backend.group.application.port.out;

import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.group.domain.GroupDiary;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupDiaryRepositoryPort {

    GroupDiary save(GroupDiary groupDiary, Diary dairy);

    List<GroupDiary> findGroupDiariesWithCursor(
            Integer groupId,
            LocalDateTime cursor,
            Pageable pageable
    );

    List<GroupDiary> findByGroupIdsWithCursorExcludeUser(
            List<Integer> groupIds,
            LocalDateTime cursor,
            Integer userId,
            Pageable pageable
    );

    Optional<GroupDiary> findByGroupIdAndDiaryId(Integer groupId, Integer diaryId);

    boolean existsByGroupIdAndDiaryId(Integer groupId, Integer diaryId);

    GroupDiary link(Integer diaryId, Integer groupId, LocalDateTime sharedAt);

}
