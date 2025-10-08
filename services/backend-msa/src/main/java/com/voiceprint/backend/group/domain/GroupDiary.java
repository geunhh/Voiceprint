package com.voiceprint.backend.group.domain;

import com.voiceprint.backend.diary.domain.Diary;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GroupDiary {

    private final Integer id;
    private final Diary diary;
    private final Group group;
    private final LocalDateTime sharedAt;

    @Builder(toBuilder = true)
    public GroupDiary(Integer id, Diary diary, Group group, LocalDateTime sharedAt) {
        this.id = id;
        this.diary = diary;
        this.group = group;
        this.sharedAt = sharedAt;
    }

    // 팩토리 메소드
    public static GroupDiary create(Diary diary, Group group) {
        return GroupDiary.builder()
                .diary(diary)
                .group(group)
                .sharedAt(LocalDateTime.now()) // Set sharedAt upon creation
                .build();
    }
}