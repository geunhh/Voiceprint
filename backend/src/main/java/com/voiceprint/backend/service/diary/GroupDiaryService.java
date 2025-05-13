package com.voiceprint.backend.service.diary;

import com.voiceprint.backend.common.exception.diary.UnauthorizedDiaryException;
import com.voiceprint.backend.domain.Entity.GroupDiary;
import com.voiceprint.backend.domain.Repository.GroupDiaryRepository;
import com.voiceprint.backend.domain.Repository.GroupRepository;
import com.voiceprint.backend.domain.diary.Diary;
import com.voiceprint.backend.domain.diary.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class GroupDiaryService {
    private final GroupDiaryRepository groupDiaryRepository;
    private final GroupRepository groupRepository;
    private final DiaryRepository diaryRepository;
    @Transactional(readOnly = false)
    public void saveSharedDiary(Long diaryId, Long userId, List<Long> groupIds) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow();
        if (!diary.getUser().getId().equals(userId)) {
            throw new UnauthorizedDiaryException("해당 일기에 권한이 없습니다.");
        }
        List<GroupDiary> groupDiaries = groupIds.stream()
                .map(groupId -> new GroupDiary(null, diary, groupRepository.findById(groupId).orElseThrow(), LocalDateTime.now()))
                .collect(Collectors.toList());

        groupDiaryRepository.saveAll(groupDiaries);
    }
}
