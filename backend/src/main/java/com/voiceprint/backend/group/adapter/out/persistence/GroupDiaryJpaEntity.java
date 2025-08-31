package com.voiceprint.backend.group.adapter.out.persistence;

import com.voiceprint.backend.diary.adapter.out.persistence.DiaryEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "GroupDiary")
@Table(name = "group_diaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDiaryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private DiaryEntity diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupJpaEntity group;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime sharedAt = LocalDateTime.now();

}

